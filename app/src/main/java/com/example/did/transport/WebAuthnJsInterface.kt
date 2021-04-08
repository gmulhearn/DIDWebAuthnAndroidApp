package com.example.did.transport

import android.webkit.JavascriptInterface
import androidx.annotation.Keep
import com.example.did.data.PublicKeyCredentialCreationOptions
import com.example.did.protocols.getChallenge
import com.example.did.protocols.getId
import com.google.gson.Gson

@Keep
class WebAuthnJsInterface {

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
    }
}