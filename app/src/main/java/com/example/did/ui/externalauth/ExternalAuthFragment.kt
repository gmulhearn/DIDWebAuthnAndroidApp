package com.example.did.ui.externalauth

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.AndroidSupportInjection
import androidx.fragment.app.Fragment
import com.budiyev.android.codescanner.*
import javax.inject.Inject
import com.example.did.R
import kotlinx.android.synthetic.main.fragment_external_auth.*

/**
 * ExternalAuth VIPER Fragment Implementation
 */
class ExternalAuthFragment : Fragment(), ExternalAuthContract.View {

    @Inject
    internal lateinit var presenter: ExternalAuthContract.Presenter

    private lateinit var codeScanner: CodeScanner

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_external_auth, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)

        // TODO setup view, event listeners etc.

        val scannerView = requireActivity().findViewById<CodeScannerView>(R.id.extAuthScannerPreview)
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

    override fun showQR(imageBitmap: Bitmap) {
        authResponseQR.setImageBitmap(imageBitmap)
    }

    override fun hideCamera() {
        extAuthScannerContainer.visibility = View.GONE
    }

    // endregion

    // region View contract

    // TODO Add view contract overrides

    // endregion

}
