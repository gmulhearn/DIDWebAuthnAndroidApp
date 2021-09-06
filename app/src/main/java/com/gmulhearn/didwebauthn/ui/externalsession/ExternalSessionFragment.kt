package com.gmulhearn.didwebauthn.ui.externalsession

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import dagger.android.support.AndroidSupportInjection
import androidx.fragment.app.Fragment
import com.budiyev.android.codescanner.*
import javax.inject.Inject
import com.gmulhearn.didwebauthn.R
import com.gmulhearn.didwebauthn.core.transport.WebRTCJsInterface
import com.gmulhearn.didwebauthn.showAlertDialog
import kotlinx.android.synthetic.main.fragment_external_session.*

/**
 * ExternalSession VIPER Fragment Implementation
 */
class ExternalSessionFragment : Fragment(), ExternalSessionContract.View {

    @Inject
    internal lateinit var presenter: ExternalSessionContract.Presenter

    private lateinit var codeScanner: CodeScanner

    companion object {
        val JS_INTERFACE_NAME = "webrtcInterface"
    }

    // region viper lifecycle

    override fun onAttach(context: Context) {
        inject()
        super.onAttach(context)
    }

    internal fun inject() {
        AndroidSupportInjection.inject(this)
    }

    // endregion

    // region view setup and state lifecycle

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_external_session, container, false)

        val wv = v.findViewById<WebView>(R.id.sessionWebView)

        wv.settings.javaScriptEnabled = true
        wv.settings.allowFileAccessFromFileURLs = true
        wv.settings.allowUniversalAccessFromFileURLs = true

        wv.addJavascriptInterface(
            WebRTCJsInterface({
                println("onsig")
                println(it)
                presenter.onClientSignalled(it)
            }, {
                println("onmsg")
                println(it)
                presenter.onServerMessage(it)
            }),
            JS_INTERFACE_NAME
        )

        wv.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                requireActivity().runOnUiThread {
                    println(request)
                    if (request?.origin.toString() == "file:///") {
                        println("granted")
                        request?.grant(request.resources)
                    } else {
                        request?.deny()
                    }
                }
            }
        }

        wv.loadUrl("file:///android_asset/webrtc/index.html")

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)

        // set up qr code
        val scannerView =
            requireActivity().findViewById<CodeScannerView>(R.id.extSessionScannerPreview)
        codeScanner = CodeScanner(requireContext(), scannerView)
    }

    override fun onResume() {
        super.onResume()
        presenter.viewLoaded(null)
    }

    override fun onPause() {
        if (::codeScanner.isInitialized) {
            codeScanner.releaseResources()
        }
        super.onPause()
    }

    override fun onDestroyView() {
        presenter.detachView()
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenter.saveState(outState)
    }

    override fun signalClient(sig: String) {
        sessionWebView.evaluateJavascript("signalPeer($sig)", null)
    }

    // endregion

    // region View contract

    override fun setupCamera() {
        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS
        codeScanner.autoFocusMode = AutoFocusMode.SAFE
        codeScanner.scanMode = ScanMode.SINGLE
        codeScanner.isAutoFocusEnabled = true
        codeScanner.isFlashEnabled = false

        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {
            requireActivity().runOnUiThread {
                println("QR code scanned: ${it.text}")
                presenter.qrCodeRead(it.text)
            }
        }
        codeScanner.errorCallback = ErrorCallback.SUPPRESS

        codeScanner.startPreview()
    }

    override fun hideCamera() {
        extSessionScannerContainer.visibility = View.GONE
        scanTitle.visibility = View.GONE
    }

    override fun showConnected() {
        connectedPrompt.visibility = View.VISIBLE
    }

    override fun sendMessageInWebView(jsonData: String) {
        println("sending msg to webview: $jsonData")

        sessionWebView.post {
            sessionWebView.evaluateJavascript("""sendData('$jsonData')""", null)
        }
    }

    override fun showUserPrompt(title: String, message: String, onConfirmation: () -> Unit) {
        showAlertDialog(
            title = title,
            message = message,
            positiveButtonResId = R.string.accept,
            onPositive = onConfirmation,
            negativeButtonResId = R.string.reject
        )
    }

    // endregion

}
