package com.gmulhearn.didwebauthn.core.transport

import android.webkit.JavascriptInterface
import androidx.annotation.Keep
import com.gmulhearn.didwebauthn.data.CollectedClientData
import com.gmulhearn.didwebauthn.data.PublicKeyCredentialCreationOptions
import com.gmulhearn.didwebauthn.data.PublicKeyCredentialRequestOptions
import com.gmulhearn.didwebauthn.core.protocols.*
import com.google.gson.Gson
import java.util.*

@Keep
class WebAuthnJsInterface(private val bridge: WebAuthnBridgeWebView) {

    @JavascriptInterface
    fun publicKeyCredentialCreate(data: String) {
        println(data)
        val opts = Gson().fromJson(data, PublicKeyCredentialCreationOptions::class.java)
        println(opts)
        println(opts.publicKey.getChallenge().toUByteArray().toList())
        println(opts.publicKey.user.getId().toUByteArray().toList())
        val makeCredOpts = opts.publicKey.toAuthenticatorMakeCredentialOptions(bridge.origin)
        val clientData = CollectedClientData(
            type = "webauthn.create",
            challengeBase64URL = Base64.getUrlEncoder().encodeToString(opts.publicKey.getChallenge()).removeSuffix("="),
            origin = bridge.origin
        )
        bridge.resolveAttestation(bridge.authenticator.makeCredentials(
            makeCredOpts,
            clientData.JSON()
        ))
    }

    @JavascriptInterface
    fun publicKeyCredentialGet(data: String) {
        println(data)
        val opts = Gson().fromJson(data, PublicKeyCredentialRequestOptions::class.java)
        println(opts)
        val getAssertionOpts = opts.publicKey.toAuthenticatorGetAssertionOptions(bridge.origin)
        val clientData = CollectedClientData(
            type = "webauthn.get",
            challengeBase64URL = Base64.getUrlEncoder().encodeToString(opts.publicKey.getChallenge()).removeSuffix("="),
            origin = bridge.origin
        )

        bridge.resolveAssertion(bridge.authenticator.getAssertion(
            getAssertionOpts,
            clientData.JSON()
        ))
    }
}