package com.example.did.protocols

import android.content.Context
import android.util.Pair
import com.example.did.data.AuthenticatorMakeCredentialOptions
import duo.labs.webauthn.Authenticator

class DIDAuthenticator(
    private val context: Context
) {
    fun makeCredentials(credOpts: AuthenticatorMakeCredentialOptions) {
        duoLabsMakeCredentials(credOpts)
    }

    private fun duoLabsMakeCredentials(credOpts: AuthenticatorMakeCredentialOptions) {
        val duoLabsMakeCreds = credOpts.toDuoLabsAuthn()
        println(duoLabsMakeCreds.credTypesAndPubKeyAlgs)
        println(duoLabsMakeCreds.credTypesAndPubKeyAlgs.add(Pair<String, Long>("public-key", -7))) // TODO
        val duoLabsAuthn = Authenticator(context, false, false)
        val attestationObject = duoLabsAuthn.makeCredential(duoLabsMakeCreds)
        println(attestationObject)
        println(attestationObject.credentialId)
        println(attestationObject.credentialIdBase64)
        println(attestationObject.asCBOR())
    }
}