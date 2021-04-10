package com.example.did.transport

import android.content.Context
import android.os.Handler
import android.webkit.WebView
import com.example.did.data.AuthenticatorAssertionResponse
import com.example.did.data.AuthenticatorAttestationResponse
import com.example.did.data.PublicKeyCredentialAssertionResponse
import com.example.did.data.PublicKeyCredentialAttestationResponse
import com.example.did.protocols.DIDAuthenticator
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

    var origin: String = ""
    private var loading = false
    public val authenticator = DIDAuthenticator(context)

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
                context.assets.open("WebAuthnBridge.js").reader(Charsets.UTF_8).readText()
            println("evaluating JS: $bridgeJS")
            webView.evaluateJavascript("javascript:($bridgeJS)()", null)
        } catch (e: IOException) {

        }
    }

    fun JSResolve(publicKeyCredentialAttestationResponse: PublicKeyCredentialAttestationResponse) {
        println(publicKeyCredentialAttestationResponse)
        val pkcString = Gson().toJson(publicKeyCredentialAttestationResponse)
        val hwSecurityJSONObj = publicKeyCredentialToJson(publicKeyCredentialAttestationResponse)
        println(hwSecurityJSONObj)
        webView.post {
            webView.evaluateJavascript(
                "javascript:webauthnbridge.handleResolve($hwSecurityJSONObj)",
                null
            )
        }
    }

    fun JSResolve(publicKeyCredentialAssertionResponse: PublicKeyCredentialAssertionResponse) {
        println(publicKeyCredentialAssertionResponse)

        val hwSecurityJSONObj = publicKeyCredentialAssertionToJson(publicKeyCredentialAssertionResponse)
        println(hwSecurityJSONObj)

        webView.post {
            webView.evaluateJavascript(
                "javascript:webauthnbridge.handleResolve($hwSecurityJSONObj)",
                null
            )
        }
    }


    /**
     * TODO: HWSECURITY
     */
    private fun publicKeyCredentialToJson(publicKeyCredentialAttestationResponse: PublicKeyCredentialAttestationResponse): JSONObject? {
        return try {
            val result = JSONObject()
            result.put("type", publicKeyCredentialAttestationResponse.type)
            result.put("id", publicKeyCredentialAttestationResponse.id)
            result.put(
                "response", authenticatorResponseToJson(
                    publicKeyCredentialAttestationResponse.response
                )
            )
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
                authenticatorResponse
            val attestationObjectB64: String = Base64.getUrlEncoder().encodeToString(
                authenticatorAttestationResponse.attestationObject
            )
            result.put("attestationObjectB64", attestationObjectB64)
            result
        } catch (e: JSONException) {
            throw java.lang.IllegalArgumentException(e)
        }
    }

    /**
     * TODO hwsecurity
     */
    private fun publicKeyCredentialAssertionToJson(publicKeyCredential: PublicKeyCredentialAssertionResponse): JSONObject? {
        return try {
            val result = JSONObject()
            result.put("type", publicKeyCredential.type)
            result.put("id", publicKeyCredential.id)
            result.put(
                "response",
                authenticatorAssertionResponseToJson(publicKeyCredential.response)
            )
            result
        } catch (e: JSONException) {
            throw java.lang.IllegalArgumentException(e)
        }
    }

    /**
     * TODO hwsecurity
     */
    private fun authenticatorAssertionResponseToJson(authenticatorResponse: AuthenticatorAssertionResponse): JSONObject? {
        return try {
            val result = JSONObject()
            result.put(
                "clientDataJsonB64",
                Base64.getUrlEncoder().encodeToString(authenticatorResponse.clientDataJSON)
            )

            val authenticatorAttestationResponse: AuthenticatorAssertionResponse =
                authenticatorResponse
            val authenticatorDataB64: String = Base64.getUrlEncoder().encodeToString(
                authenticatorAttestationResponse.authenticatorData
            )
            result.put("authenticatorDataB64", authenticatorDataB64)
            val signatureB64: String = Base64.getUrlEncoder().encodeToString(
                authenticatorAttestationResponse.signature
            )
            result.put("signatureB64", signatureB64)
            val userHandle: ByteArray = authenticatorAttestationResponse.userHandle
            if (userHandle != null) {
                val userHandleB64: String = Base64.getUrlEncoder().encodeToString(userHandle)
                result.put("userHandleB64", userHandleB64)
            }

            result
        } catch (e: JSONException) {
            throw java.lang.IllegalArgumentException(e)
        }
    }
}