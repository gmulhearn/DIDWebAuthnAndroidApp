package com.example.did.transport

import android.content.Context
import android.os.Handler
import android.webkit.WebView
import java.io.IOException

class WebAuthnBridgeWebView(
    private val context: Context,
    private val webView: WebView
) {
    companion object {
        val BRIDGE_INTERFACE = "webauthnbridgekt"
        val BRIDGE_JS = "WebAuthnBridge.js"
    }

    private var origin: String? = null
    private var loading = false

    fun bindWebView() {
        webView.addJavascriptInterface(WebAuthnJsInterface(), BRIDGE_INTERFACE)
    }

    fun onWebViewRequest() {
        if (loading) {
            loading = false
            val handler = Handler(context.mainLooper)
            handler.postAtFrontOfQueue(this::injectJS)
        }
    }

    private fun injectJS() {
        try {
            val bridgeJS = context.assets.open("WebAuthnBridge.js").reader(Charsets.UTF_8).readText()
            println("evaluating JS: $bridgeJS")
            webView.evaluateJavascript("javascript:($bridgeJS)()", null)
        } catch (e : IOException) {

        }
    }

    fun onPageStart() {
        loading = true
    }
}