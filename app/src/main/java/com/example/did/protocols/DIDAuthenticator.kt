package com.example.did.protocols

import android.content.Context
import android.util.Pair
import com.example.did.data.AuthenticatorMakeCredentialOptions
import com.example.did.data.PublicKeyCredential
import duo.labs.webauthn.Authenticator

class DIDAuthenticator(
    private val context: Context
) {
    fun makeCredentials(
        credOpts: AuthenticatorMakeCredentialOptions,
        clientDataJson: String
    ): PublicKeyCredential {
        return duoLabsMakeCredentials(credOpts, clientDataJson)
    }

    private fun duoLabsMakeCredentials(
        credOpts: AuthenticatorMakeCredentialOptions,
        clientDataJson: String
    ): PublicKeyCredential {
        val duoLabsMakeCreds = credOpts.toDuoLabsAuthn()
        println(duoLabsMakeCreds.credTypesAndPubKeyAlgs)
        println(
            duoLabsMakeCreds.credTypesAndPubKeyAlgs.add(
                Pair<String, Long>(
                    "public-key",
                    -7
                )
            )
        ) // TODO
        val duoLabsAuthn = Authenticator(context, false, false)
        val attestationObject = duoLabsAuthn.makeCredential(duoLabsMakeCreds)
        println(attestationObject)
        println(attestationObject.credentialId)
        println(attestationObject.credentialIdBase64)
        println(attestationObject.asCBOR())
        return createPublicKeyCredential(
            rawId = attestationObject.credentialId,
            attestationObject = attestationObject.asCBOR(),
            clientDataJson = clientDataJson.toByteArray(Charsets.UTF_8)
        )
    }
}