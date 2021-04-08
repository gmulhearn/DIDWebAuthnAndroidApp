package com.example.did.data

import com.google.gson.annotations.SerializedName

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