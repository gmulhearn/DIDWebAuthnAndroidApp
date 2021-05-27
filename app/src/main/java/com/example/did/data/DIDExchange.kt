package com.example.did.data

import com.google.gson.annotations.SerializedName
import org.spongycastle.crypto.util.Pack

data class Invitation(
    @SerializedName("@type") val type: String = "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/connections/1.0/invitation",
    @SerializedName("@id") val id: String,
    val label: String,
    val recipientKeys: List<String>,
    val serviceEndpoint: String,
    val routingKeys: List<String>?
)

data class DIDCommContainer(
    val message: Any,
    @SerializedName("recipient_verkey") val recipientVerkey: String,
    @SerializedName("sender_verkey") val senderVerkey: String
)

data class PackedMessage(
    val protected: String,
    val ciphertext: String,
    val iv: String,
    val tag: String
)

data class RoutingForwardMessage(
    @SerializedName("@type") val type: String = "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/routing/1.0/forward",
    @SerializedName("@id") val id: String,
    val to: String,
    @SerializedName("msg") val message: PackedMessage
)

data class DIDRequestMessage(
    val label: String,
    val connection: DIDRequestConnection,
    @SerializedName("@type") val type: String,
    @SerializedName("@id") val id: String
)

data class DIDRequestConnection(
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
    val routingKeys: List<String>? = listOf(),
    val type: String = "IndyAgent",
    val serviceEndpoint: String,
    val recipientKeys: List<String>
)

data class DIDResponseMessage(
    @SerializedName("connection~sig") val connectionSig: DIDResponseConnectionSig,
    @SerializedName("~thread") val thread: DIDResponseThread,
    @SerializedName("@type") val type: String = "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/connections/1.0/response",
    @SerializedName("@id") val id: String
)

data class DIDResponseConnectionSig(
    val signer: String,
    val signature: String,
    @SerializedName("sig_data") val sigData: String,
    @SerializedName("@type") val type: String = "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/signature/1.0/ed25519Sha512_single"
)

data class DIDResponseThread(
    val thid: String
)

data class DIDCommMessage(
    @SerializedName("sent_time") val sentTime: String,
    val content: String,
    @SerializedName("@type") val type: String = "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/basicmessage/1.0/message",
    @SerializedName("@id") val id: String
)

enum class ProtocolStage {
    SCANNED_QR,
    REPLYING,
    RESPONDING,
    PROCESSING_RESPONSE,
    SUCCESS,
    FAILED
}