package com.example.did.transport

import android.content.Context
import android.os.Handler
import android.webkit.WebView
import com.example.did.data.AuthenticatorAttestationResponse
import com.example.did.data.PublicKeyCredential
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

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
        webView.addJavascriptInterface(WebAuthnJsInterface(context, this), BRIDGE_INTERFACE)
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
            val bridgeJS =
                context.assets.open("WebAuthnBridge.js").reader(Charsets.UTF_8).readText()
            println("evaluating JS: $bridgeJS")
            webView.evaluateJavascript("javascript:($bridgeJS)()", null)
        } catch (e: IOException) {

        }
    }

    fun JSResolve(publicKeyCredential: PublicKeyCredential) {
        println(publicKeyCredential)
        val pkcString = Gson().toJson(publicKeyCredential)
        val hwSecurityJSONObj = publicKeyCredentialToJson(publicKeyCredential)
        println(hwSecurityJSONObj)
        webView.post {
            webView.evaluateJavascript("javascript:webauthnbridge.handleResolve($hwSecurityJSONObj)", null)
        }
    }

    /**
     * TODO: HWSECURITY
     */
    private fun publicKeyCredentialToJson(publicKeyCredential: PublicKeyCredential): JSONObject? {
        return try {
            val result = JSONObject()
            result.put("type", publicKeyCredential.type)
            result.put("id", publicKeyCredential.id)
            result.put("response", authenticatorResponseToJson(publicKeyCredential.response))
            result
        } catch (e: JSONException) {
            throw IllegalArgumentException(e)
        }
    }

    /**
     * TODO: HWSECURITY
     */
    private fun authenticatorResponseToJson(authenticatorResponse: AuthenticatorAttestationResponse): JSONObject? {
        return try {
            val result = JSONObject()
            result.put(
                "clientDataJsonB64",
                Base64.getUrlEncoder().encodeToString(authenticatorResponse.clientDataJSON)
            )

            val authenticatorAttestationResponse: AuthenticatorAttestationResponse =
                authenticatorResponse as AuthenticatorAttestationResponse
            val attestationObjectB64: String = Base64.getUrlEncoder().encodeToString(
                authenticatorAttestationResponse.attestationObject
            )
            result.put("attestationObjectB64", attestationObjectB64)
            result
        } catch (e: JSONException) {
            throw java.lang.IllegalArgumentException(e)
        }
    }


    fun onPageStart() {
        loading = true
    }
}