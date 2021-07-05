package com.gmulhearn.didwebauthn.ui.browser

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.SearchView
import dagger.android.support.AndroidSupportInjection
import androidx.fragment.app.Fragment
import com.gmulhearn.didwebauthn.R
import com.gmulhearn.didwebauthn.common.WalletProvider
import com.gmulhearn.didwebauthn.transport.WebAuthnBridgeWebView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_browser.*
import javax.inject.Inject

/**
 * Browser VIPER Fragment Implementation
 */
class BrowserFragment : Fragment(), BrowserContract.View, SearchView.OnQueryTextListener {

    @Inject
    internal lateinit var presenter: BrowserContract.Presenter
    internal lateinit var webAuthnBridge: WebAuthnBridgeWebView

    @Inject
    internal lateinit var walletProvider: WalletProvider

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
        val v =  inflater.inflate(R.layout.fragment_browser, container, false)

        val webViewer = v.findViewById<WebView>(R.id.webView)
        initInjectedWebView(webViewer)

        return v
    }

    private fun initInjectedWebView(webViewer: WebView) {
        webViewer.settings.javaScriptEnabled = true
        webViewer.loadUrl("https://webauthn.io")
        webAuthnBridge = WebAuthnBridgeWebView(this.requireContext(), webViewer, walletProvider)
        webAuthnBridge.bindWebView()

        webViewer.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                println("REQUEST: \n\t${request?.method}\n\t${request?.requestHeaders}")
                webAuthnBridge.onWebViewRequest()
                return super.shouldInterceptRequest(view, request)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                println("PAGE STARTED")
                webAuthnBridge.onPageStart(url)
                super.onPageStarted(view, url, favicon)
            }
        }

        webViewer.webChromeClient = object : WebChromeClient() {
            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                showSnackbar("alert: $message")
                return super.onJsAlert(view, url, message, result)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)

        searchbar.setOnSearchClickListener {
            println(searchbar.query)
        }
        searchbar.setOnQueryTextListener(this)
        // Notify Presenter that the View is ready
        presenter.viewLoaded(savedInstanceState)
    }

    override fun onDestroyView() {
        presenter.detachView()
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenter.saveState(outState)
    }

    // SEARCHBAR LISTENER
    override fun onQueryTextSubmit(query: String?): Boolean {
        query?.let {
            presenter.querySubmitted(it)
            // webView.loadUrl(query)
        }
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return false
    }
    // END SEARCHBAR LISTENER


    private fun showSnackbar(status: String) {
        Snackbar.make(this.requireView(), status, Snackbar.LENGTH_LONG).show()
    }

    // endregion

    // region View contract

    override fun loadUrl(url: String) {
        webView.loadUrl(url)
    }

    // endregion
}

