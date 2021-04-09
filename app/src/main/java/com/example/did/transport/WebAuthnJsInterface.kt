package com.example.did.transport

import android.content.Context
import android.webkit.JavascriptInterface
import androidx.annotation.Keep
import com.example.did.data.CollectedClientData
import com.example.did.data.PublicKeyCredentialCreationOptions
import com.example.did.protocols.*
import com.google.gson.Gson
import java.util.*

@Keep
class WebAuthnJsInterface(private val context: Context, private val parent: WebAuthnBridgeWebView) {

    @JavascriptInterface
    fun publicKeyCredentialGet(data: String) {
        println(data)
    }

    @JavascriptInterface
    fun publicKeyCredentialCreate(data: String) {
        println(data)
        val opts = Gson().fromJson(data, PublicKeyCredentialCreationOptions::class.java)
        println(opts)
        println(opts.publicKey.getChallenge().toUByteArray().toList())
        println(opts.publicKey.user.getId().toUByteArray().toList())
        val makeCredOpts = opts.publicKey.toAuthenticatorMakeCredentialOptions("webauthn.io") // TODO
        val clientData = CollectedClientData(
            type = "webauthn.create",
            challengeBase64URL = Base64.getUrlEncoder().encodeToString(opts.publicKey.getChallenge()),
            origin = "webauthn.io" // TODO
        )
        parent.JSResolve(DIDAuthenticator(context).makeCredentials(
            makeCredOpts,
            clientData.JSON()
        ))
    }
}