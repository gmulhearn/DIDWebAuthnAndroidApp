package com.example.did.data

import com.google.gson.annotations.SerializedName

data class Invitation(
    @SerializedName("@type") val type: String = "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/connections/1.0/invitation",
    @SerializedName("@id") val id: String,
    val label: String,
    val recipientKeys: List<String>,
    val serviceEndpoint: String,
    val routingKeys: List<String>
)

data class Message(
    val message: Any,
    @SerializedName("recipient_verkey") val recipientVerkey: String,
    @SerializedName("sender_verkey") val senderVerkey: String
)

data class DIDCommMessage(
    val label: String,
    val connection: Any,
    @SerializedName("@type") val type: String,
    @SerializedName("@id") val id: String
)

data class DIDRequest(
    @SerializedName("DID") val did: String,
    @SerializedName("DIDDoc") val didDoc: DIDDoc
)

data class DIDDoc(
    val id: String,
    val publicKey: List<DIDDocPublicKey>,
    @SerializedName("@context") val context: String = "https://www.w3.org/ns/did/v1",
    val service: List<DIDDocService>

)

data class DIDDocPublicKey(
    val id: String,
    val publicKeyBase58: String,
    val type: String = "Ed25519VerificationKey2018",
    val controller: String
)

data class DIDDocService(
    val id: String,
    val routingKeys: List<String> = listOf(),
    val type: String = "IndyAgent",
    val serviceEndpoint: String,
    val recipientKeys: List<String>
)