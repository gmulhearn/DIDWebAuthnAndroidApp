package com.example.did.data

import com.google.gson.annotations.SerializedName

/************************ WEBAUTHN **************************/

data class RelyingPartyInfo(
    val name: String,
    val id: String
)

data class UserInfo(
    val name: String,
    val displayName: String,
    @SerializedName("id") val mappedId: Map<String, Int>
)

data class PublicKeyCredentialParameters(
    val type: String,
    @SerializedName("alg") val algorithm: Int
)

data class AuthenticatorSelection(
    val requiresResidentKey: Boolean,
    val userVerification: String
)

data class CredentialCreationOptions(
    @SerializedName("challenge") val mappedChallenge: Map<String, Int>,
    @SerializedName("rp") val relyingPartyInfo: RelyingPartyInfo,
    val user: UserInfo,
    val pubKeyCredParams: List<PublicKeyCredentialParameters>,
    val authenticatorSelection: AuthenticatorSelection,
    val timeout: Int,
    val extensions: Map<String, String>,
    val attestation: String
)

data class PublicKeyCredentialCreationOptions(
    val publicKey: CredentialCreationOptions
)

/************************ WEBAUTHN **************************/


/************************** CTAP ****************************/

data class PublicKeyCredentialRpEntity(
    val id: String,
    val name: String
)

data class PublicKeyCredentialUserEntity(
    val id: ByteArray,
    val displayName: String,
    val name: String
)

data class AuthenticatorMakeCredentialOptions(
    val clientDataHash: ByteArray,
    val rp: PublicKeyCredentialRpEntity,
    val user: PublicKeyCredentialUserEntity,
    val pubKeyCredParams: List<Pair<String, Long>>
)

data class CollectedClientData(
    val type: String,   // "webauthn.create" or "webauthn.get"
    @SerializedName("challenge") val challengeBase64URL: String,
    val origin: String,
    val crossOrigin: Boolean = false
)

data class AuthenticatorAttestationResponse(
    val clientDataJSON: ByteArray,
    val attestationObject: ByteArray
)

data class PublicKeyCredential(
    val rawId: ByteArray,
    val id: String,
    val type: String = "public-key",
    val response: AuthenticatorAttestationResponse
)

/************************** CTAP ****************************/