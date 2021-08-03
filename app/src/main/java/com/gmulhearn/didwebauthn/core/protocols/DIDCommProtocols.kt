package com.gmulhearn.didwebauthn.core.protocols

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.gmulhearn.didwebauthn.data.*
import com.gmulhearn.didwebauthn.core.transport.relay.RelayRepository
import com.google.gson.Gson
import org.hyperledger.indy.sdk.crypto.Crypto
import org.hyperledger.indy.sdk.did.Did
import org.hyperledger.indy.sdk.wallet.Wallet
import java.nio.ByteBuffer
import java.util.*

class DIDCommProtocols (private val relay: RelayRepository) {

    @SuppressLint("HardwareIds")
    fun generateInvitation(
        wallet: Wallet,
        did: DidInfo,
        context: Context,
        label: String
    ): Invitation {
        val didKey = Did.keyForLocalDid(wallet, did.did).get()
        val androidId =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        val endpoint = relay.getServiceEndpoint(androidId)

        return Invitation(
            id = androidId,
            label = label,
            recipientKeys = listOf(didKey),
            serviceEndpoint = endpoint,
            routingKeys = listOf()
        )
    }

    fun generateDIDDoc(did: DidInfo, context: Context): DIDDoc {
        val androidId =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        val endpoint = relay.getServiceEndpoint(androidId)

        val publicKey = DIDDocPublicKey(
            id = did.did + "#keys-1",
            publicKeyBase58 = did.verkey,
            controller = did.did
        )

        val service = DIDDocService(
            id = did.did + ";indy",
            routingKeys = listOf(),
            serviceEndpoint = endpoint,
            recipientKeys = listOf(did.verkey)
        )

        return DIDDoc(
            id = did.did,
            publicKey = listOf(publicKey),
            service = listOf(service)
        )
    }

    fun routingForwardWrap(
        packedMessage: PackedMessage,
        myWallet: Wallet,
        nextRecipientKey: String,
        routingKeys: List<String>,
        context: Context
    ): PackedMessage {

        val currentRoutingKey =
            routingKeys.firstOrNull() ?: return packedMessage  // end of wrapping
        val androidId =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

        val routingForwardMessage = RoutingForwardMessage(
            id = androidId,
            to = nextRecipientKey,
            message = packedMessage
        )

        val jsonMessage = Gson().toJson(routingForwardMessage).replace("""\u003d""", "=")
        println(
            """
            DEBUG ROUTING FORWARD (P2):
            $jsonMessage
        """.trimIndent()
        )

        val wrappedPackedMessageRaw = Crypto.packMessage(
            myWallet,
            "[\"${currentRoutingKey}\"]",
            null,
            jsonMessage.toByteArray(Charsets.UTF_8)
        ).get()

        val wrappedPackedMessage = Gson().fromJson(
            wrappedPackedMessageRaw.toString(Charsets.UTF_8),
            PackedMessage::class.java
        )

        return routingForwardWrap(
            wrappedPackedMessage,
            myWallet,
            currentRoutingKey,
            routingKeys.drop(1),
            context
        )
    }

    /**
     * Helper function for generating raw byte array of packed routed message from raw byte array of original packed message
     */
    private fun packedBytesToRoutedPackedBytes(
        rawMessagePacked: ByteArray,
        myWallet: Wallet,
        theirVerkey: String,
        theirRoutingKeys: List<String>,
        context: Context
    ): ByteArray {
        val messagePacked =
            Gson().fromJson(rawMessagePacked.toString(Charsets.UTF_8), PackedMessage::class.java)
        val routingWrappedMessage = routingForwardWrap(
            messagePacked,
            myWallet,
            theirVerkey,
            theirRoutingKeys,
            context
        )

        val rawRoutingForwardMessage =
            Gson().toJson(routingWrappedMessage).replace("""\u003d""", "=")
        return rawRoutingForwardMessage.toByteArray(Charsets.UTF_8)
    }

    private fun generateRequest(label: String, did: DidInfo, context: Context): DIDRequestMessage {
        val androidId =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

        val didDoc = generateDIDDoc(did, context)

        val didRequestConnection = DIDRequestConnection(
            did = did.did,
            didDoc = didDoc
        )

        return DIDRequestMessage(
            label = label,
            connection = didRequestConnection,
            type = "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/connections/1.0/request",
            id = androidId
        )
    }

    fun generateEncryptedRequestMessage(
        label: String,
        myWallet: Wallet,
        myDid: DidInfo,
        theirDid: DidInfo,
        theirRoutingKeys: List<String>,
        context: Context
    ): ByteArray {
        val didCommMessage = generateRequest(label, myDid, context)

        val jsonMessage = Gson().toJson(didCommMessage).replace("""\u003d""", "=")

        println(didCommMessage)
        println(jsonMessage)

        val rawMessagePacked = Crypto.packMessage(
            myWallet,
            "[\"${theirDid.verkey}\"]",
            myDid.verkey,
            jsonMessage.toByteArray(Charsets.UTF_8)
        ).get()

        return packedBytesToRoutedPackedBytes(
            rawMessagePacked,
            myWallet,
            theirDid.verkey,
            theirRoutingKeys,
            context
        )
    }

    fun generateEncryptedDIDCommMessage(
        myWallet: Wallet,
        pairwiseContact: PairwiseContact,
        time: String,
        message: String,
        context: Context
    ): ByteArray {

        val androidId =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        val didCommMessage = DIDCommMessage(
            sentTime = time,
            content = message,
            id = androidId
        )

        val jsonMessage = Gson().toJson(didCommMessage)
        println("jsonMessage: $jsonMessage")

        val rawMessagePacked = Crypto.packMessage(
            myWallet,
            "[\"${pairwiseContact.metadata.theirVerkey}\"]",
            pairwiseContact.metadata.myVerkey,
            jsonMessage.toByteArray(Charsets.UTF_8)
        ).get()

        return packedBytesToRoutedPackedBytes(
            rawMessagePacked,
            myWallet,
            pairwiseContact.metadata.theirVerkey,
            pairwiseContact.metadata.theirRoutingKeys ?: emptyList(),
            context
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateEncryptedResponseMessage(
        myWallet: Wallet,
        myDid: DidInfo,
        theirDid: DidInfo,
        theirRoutingKeys: List<String>,
        context: Context
    ): ByteArray {
        val connection = DIDRequestConnection(
            myDid.did,
            generateDIDDoc(myDid, context)
        )

        val connectionJson = Gson().toJson(connection).replace("""\u003d""", "=")

        val timestamp = Date().time
        val buffer = ByteBuffer.allocate(8)
        val timestampBytes = buffer.putLong(timestamp).array()
        val dataToSign = timestampBytes + connectionJson.toByteArray(Charsets.UTF_8)
        val connectionBase64 =
            Base64.getUrlEncoder().encode(dataToSign).toString(Charsets.UTF_8)

        val signature =
            Base64.getUrlEncoder().encode(
                Crypto.cryptoSign(
                    myWallet,
                    myDid.verkey,
                    dataToSign // connectionBase64.toByteArray(Charsets.UTF_8)
                ).get()
            ).toString(Charsets.UTF_8)

        val connectionSig = DIDResponseConnectionSig(
            signer = myDid.verkey,
            signature = signature,
            sigData = connectionBase64
        )

        val thread = DIDResponseThread(
            thid = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        )
        val response = DIDResponseMessage(
            connectionSig,
            thread,
            id = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        )

        println(response)

        val jsonMessage = Gson().toJson(response).replace("""\u003d""", "=")

        println(jsonMessage)

        val rawMessagePacked = Crypto.packMessage(
            myWallet,
            "[\"${theirDid.verkey}\"]",
            myDid.verkey,
            jsonMessage.toByteArray(Charsets.UTF_8)
        ).get()

        return packedBytesToRoutedPackedBytes(
            rawMessagePacked,
            myWallet,
            theirDid.verkey,
            theirRoutingKeys,
            context
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateInvitationUrl(invitation: Invitation): String {
        val baseUrl = "https://ssisample.sudoplatform.com/?c_i="
        val inviteJsonString = invitation.toJsonString()
        println(inviteJsonString)
        val encodedInvite =
            Base64.getUrlEncoder().encode(inviteJsonString.toByteArray(Charsets.UTF_8))
                .toString(Charsets.UTF_8)

        return baseUrl + encodedInvite
    }
}

fun Invitation.toJsonString(): String {
    return Gson().toJson(this)
}