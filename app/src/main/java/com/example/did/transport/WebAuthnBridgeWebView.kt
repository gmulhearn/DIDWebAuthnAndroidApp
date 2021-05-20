package com.example.did.transport

import android.content.Context
import android.os.Handler
import android.webkit.WebView
import com.example.did.common.WalletProvider
import com.example.did.data.PublicKeyCredentialAssertionResponse
import com.example.did.data.PublicKeyCredentialAttestationResponse
import com.example.did.protocols.DIDAuthenticator
import com.google.gson.Gson
import java.io.IOException

class WebAuthnBridgeWebView(
    private val context: Context,
    private val webView: WebView,
    private val walletProvider: WalletProvider
) {
    companion object {
        val BRIDGE_INTERFACE = "webAuthnInterface"
        val INJECTED_JS_FILE_NAME = "InjectNavigatorCredentials.js"
    }

    var origin: String = ""
    private var loading = false
    val authenticator = DIDAuthenticator(context, walletProvider)

    fun bindWebView() {
        webView.addJavascriptInterface(WebAuthnJsInterface(this), BRIDGE_INTERFACE)
    }

    fun onWebViewRequest() {
        if (loading) {
            loading = false
            val handler = Handler(context.mainLooper)
            handler.postAtFrontOfQueue(this::injectJS)
        }
    }

    fun onPageStart(url: String?) {
        loading = true
        if (url != null) {
            origin = url
        }
    }

    private fun injectJS() {
        try {
            val bridgeJS =
                context.assets.open(INJECTED_JS_FILE_NAME).reader(Charsets.UTF_8).readText()
            webView.evaluateJavascript("javascript:($bridgeJS)()", null)
        } catch (e: IOException) {

        }
    }

    fun resolveAttestation(publicKeyCredentialAttestationResponse: PublicKeyCredentialAttestationResponse) {
        println(publicKeyCredentialAttestationResponse)
        val pkcString = Gson().toJson(publicKeyCredentialAttestationResponse)

        println(pkcString)
        webView.post {
            webView.evaluateJavascript(
                "javascript:navigator.credentials.resolveRegistration($pkcString)",
                null
            )
        }
    }

    fun resolveAssertion(publicKeyCredentialAssertionResponse: PublicKeyCredentialAssertionResponse) {
        println(publicKeyCredentialAssertionResponse)

        val pkcString = Gson().toJson(publicKeyCredentialAssertionResponse)

        webView.post {
            webView.evaluateJavascript(
                "javascript:navigator.credentials.resolveAuthentication($pkcString)",
                null
            )
        }
    }
}