package com.example.did.protocols

import android.content.Context
import android.util.Pair
import com.example.did.common.WalletProvider
import com.example.did.data.AuthenticatorGetAssertionOptions
import com.example.did.data.AuthenticatorMakeCredentialOptions
import com.example.did.data.PublicKeyCredentialAssertionResponse
import com.example.did.data.PublicKeyCredentialAttestationResponse
import duo.labs.webauthn.BasicAuthenticator
import javax.inject.Inject

class DIDAuthenticator @Inject constructor(
    val context: Context,
    val walletProvider: WalletProvider
) {
    private val duoLabsAuthn = BasicAuthenticator(context, false, false, walletProvider.getWallet())

    fun makeCredentials(
        credOpts: AuthenticatorMakeCredentialOptions,
        clientDataJson: String
    ): PublicKeyCredentialAttestationResponse {
        return duoLabsMakeCredentials(credOpts, clientDataJson)
    }

    private fun duoLabsMakeCredentials(
        credOpts: AuthenticatorMakeCredentialOptions,
        clientDataJson: String
    ): PublicKeyCredentialAttestationResponse {
        val duoLabsMakeCreds = credOpts.toDuoLabsAuthn()
        println(duoLabsMakeCreds.credTypesAndPubKeyAlgs)

        val attestationObject = duoLabsAuthn.makeCredential(duoLabsMakeCreds)
        println(attestationObject)
        println(attestationObject.credentialId)
        println(attestationObject.credentialIdBase64)
        println(attestationObject.asCBOR())
        return createPublicKeyCredentialAttestationResponse(
            rawId = attestationObject.credentialId,
            attestationObject = attestationObject.asCBOR(),
            clientDataJson = clientDataJson.toByteArray(Charsets.UTF_8)
        )
    }

    fun getAssertion(
        assertionOpts: AuthenticatorGetAssertionOptions,
        clientDataJson: String
    ): PublicKeyCredentialAssertionResponse {
        return duoLabsGetAssertion(assertionOpts, clientDataJson)
    }

    fun duoLabsGetAssertion(
        assertionOpts: AuthenticatorGetAssertionOptions,
        clientDataJson: String
    ): PublicKeyCredentialAssertionResponse {
        val duoLabsGetAssertionOpts = assertionOpts.toDuoLabAuthn()

        val assertionResult =
            duoLabsAuthn.getAssertion(duoLabsGetAssertionOpts, null)

        return createPublicKeyCredentialAssertionResponse(
            rawId = assertionResult.selectedCredentialId,
            authenticatorData = assertionResult.authenticatorData,
            clientDataJson = clientDataJson.toByteArray(Charsets.UTF_8),
            signature = assertionResult.signature,
            userHandle = assertionResult.selectedCredentialUserHandle
        )
    }
}