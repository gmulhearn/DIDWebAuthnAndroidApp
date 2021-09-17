package com.gmulhearn.didwebauthn.core.transport

import android.content.Context
import android.os.Handler
import android.webkit.WebView
import com.gmulhearn.didwebauthn.common.WalletProvider
import com.gmulhearn.didwebauthn.data.PublicKeyCredentialAssertionResponse
import com.gmulhearn.didwebauthn.data.PublicKeyCredentialAttestationResponse
import com.gmulhearn.didwebauthn.core.protocols.DIDAuthenticator
import com.google.gson.Gson
import java.io.IOException

class WebAuthnBridgeWebView(
    private val context: Context,
    private val webView: WebView,
    private val walletProvider: WalletProvider,
    val requestPermissionFromView: (title: String, message: String, onConfirmation: () -> Unit) -> Unit
) {
    companion object {
        const val BRIDGE_INTERFACE = "webAuthnInterface"
        const val INJECTED_JS_FILE_NAME = "InjectNavigatorCredentials.js"
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
            println(origin)
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
                "javascript:window.webauthnResolution = $pkcString",
                null
            )
        }
    }

    fun resolveAssertion(publicKeyCredentialAssertionResponse: PublicKeyCredentialAssertionResponse) {
        println(publicKeyCredentialAssertionResponse)

        val pkcString = Gson().toJson(publicKeyCredentialAssertionResponse)

        webView.post {
            webView.evaluateJavascript(
                "javascript:window.webauthnResolution = $pkcString",
                null
            )
        }
    }
}