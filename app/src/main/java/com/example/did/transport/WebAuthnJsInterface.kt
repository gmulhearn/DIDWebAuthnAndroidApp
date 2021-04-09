package com.example.did.transport

import android.content.Context
import android.webkit.JavascriptInterface
import androidx.annotation.Keep
import com.example.did.data.PublicKeyCredentialCreationOptions
import com.example.did.protocols.DIDAuthenticator
import com.example.did.protocols.getChallenge
import com.example.did.protocols.getId
import com.example.did.protocols.toAuthenticatorMakeCredentialOptions
import com.google.gson.Gson

@Keep
class WebAuthnJsInterface(private val context: Context) {

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
        val makeCredOpts = opts.publicKey.toAuthenticatorMakeCredentialOptions("https://todo.com")
        DIDAuthenticator(context).makeCredentials(makeCredOpts)
    }
}