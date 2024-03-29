package com.gmulhearn.didwebauthn.core.transport

import android.webkit.JavascriptInterface
import androidx.annotation.Keep
import com.gmulhearn.didwebauthn.data.PublicKeyCredentialCreationOptions
import com.gmulhearn.didwebauthn.data.PublicKeyCredentialRequestOptions
import com.gmulhearn.didwebauthn.core.protocols.*
import com.gmulhearn.didwebauthn.data.AllowCredentialDescriptor
import com.gmulhearn.didwebauthn.data.UserInfo
import com.google.gson.Gson

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
        val clientData = createCollectedClientData(
            true,
            opts.publicKey.getChallenge(),
            bridge.origin
        )

        requestUserRegistrationConfirmation(bridge.origin, opts.publicKey.user) {
            bridge.resolveAttestation(bridge.authenticator.authenticatorMakeCredential(
                rp = makeCredOpts.rp,
                user = makeCredOpts.user,
                pubKeyCredParams = makeCredOpts.pubKeyCredParams,
                clientDataJson = clientData.JSON()
            ))
        }

    }
    private fun requestUserRegistrationConfirmation(
        origin: String,
        userInfo: UserInfo,
        onConfirmation: () -> Unit
    ) {
        bridge.requestPermissionFromView(
            "Incoming WebAuthn Registration Request",
            "request from origin: $origin\nfor user: ${userInfo.displayName}",
            onConfirmation
        )
    }


    @JavascriptInterface
    fun publicKeyCredentialGet(data: String) {
        println(data)
        val opts = Gson().fromJson(data, PublicKeyCredentialRequestOptions::class.java)
        println(opts)
        val getAssertionOpts = opts.publicKey.toAuthenticatorGetAssertionOptions(bridge.origin)
        val clientData = createCollectedClientData(
            false,
            opts.publicKey.getChallenge(),
            bridge.origin
        )

        requestUserAuthenticationConfirmation(
            bridge.origin,
            opts.publicKey.allowCredentials
        ) {
            bridge.resolveAssertion(bridge.authenticator.authenticatorGetAssertion(
                rpId = getAssertionOpts.rpId,
                clientDataHash = getAssertionOpts.clientDataHash,
                allowList = getAssertionOpts.allowCredentialDescriptorList,
                clientDataJson = clientData.JSON()
            ))
        }
    }

    private fun requestUserAuthenticationConfirmation(
        origin: String,
        allowedCredentials: List<AllowCredentialDescriptor>,
        onConfirmation: () -> Unit
    ) {
        val allowedCredsString = allowedCredentials.joinToString(separator = ",\n", prefix = "- ") { it.getId().toString(Charsets.UTF_8) }
        bridge.requestPermissionFromView(
            "Incoming WebAuthn Authentication Request",
            "request from origin: $origin\nallowed keys Ids: $allowedCredsString",
            onConfirmation
        )
    }

}