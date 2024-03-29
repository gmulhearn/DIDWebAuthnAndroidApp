package com.gmulhearn.didwebauthn.ui.didcomm.chat

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.gmulhearn.didwebauthn.common.MSCoroutineScope
import com.gmulhearn.didwebauthn.common.ObjectDelegate
import com.gmulhearn.didwebauthn.common.WalletProvider
import com.gmulhearn.didwebauthn.data.*
import com.gmulhearn.didwebauthn.core.protocols.DIDCommProtocols
import com.gmulhearn.didwebauthn.core.transport.relay.RelayRepository
import com.gmulhearn.didwebauthn.data.indy.PairwiseContact
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.hyperledger.indy.sdk.crypto.Crypto
import java.util.UUID
import javax.inject.Inject

/**
 * Chat VIPER Interactor Implementation
 */
class ChatInteractor @Inject constructor(
    internal val coroutineScope: MSCoroutineScope,
    private val pairwiseContact: PairwiseContact,
    private val context: Context,
    private val walletProvider: WalletProvider,
    private val relay: RelayRepository
) : ChatContract.InteractorInput, CoroutineScope by coroutineScope {

    private val didComm = DIDCommProtocols(relay)

    internal val outputDelegate = ObjectDelegate<ChatContract.InteractorOutput>()
    internal val output by outputDelegate

    internal val wallet = walletProvider.getWallet()

    internal val messageList: MutableList<MessageDisplayModel> = mutableListOf()

    internal var chatInfoShowing: Boolean = false

    // region viper lifecycle

    override fun attachOutput(output: ChatContract.InteractorOutput) {
        outputDelegate.attach(output)
    }

    override fun detachOutput() {
        coroutineScope.cancelJobs()
        outputDelegate.detach()
    }

    override fun loadData(savedState: Bundle?) {
        launch {
            relay.subscribeToMessages(pairwiseContact.myDid) { data ->
                processMessage(data)
            }
            relay.getMessages(pairwiseContact.myDid).forEach { data ->
                processMessage(data)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun processMessage(data: ByteArray) {
        val didCommContainer: DIDCommContainer
        try {
            val unencryptedMsg =
                Crypto.unpackMessage(wallet, data).get().toString(Charsets.UTF_8)
            println("unencrypted: $unencryptedMsg")

            didCommContainer = Gson().fromJson(unencryptedMsg, DIDCommContainer::class.java)
        } catch (e: java.lang.Exception) {
            println("failed to decrypt: $e")
            return
        }

        if (didCommContainer.senderVerkey != pairwiseContact.metadata.theirVerkey) {
            println("received message but not from this contact: ${didCommContainer.senderVerkey}, ${pairwiseContact.metadata.theirVerkey}")
            // return
        }

        // Try as request
        try {
            val didCommMessage =
                Gson().fromJson(didCommContainer.message.toString(), DIDCommMessage::class.java)
            checkNotNull(didCommMessage.content)
            val isSender = didCommContainer.senderVerkey == pairwiseContact.metadata.myVerkey
            messageList.add(MessageDisplayModel(didCommMessage, isSender = isSender))
            output.updateMessages(messageList)
        } catch (e: java.lang.Exception) {
            println("failed to decode as msg $e")
        }
    }

    override fun savePendingState(outState: Bundle) {
    }

    override fun sendMessage(message: String) {
        println("sendMessage Called: $message")
        if (message.isBlank()) {
            return
        }

        val messagePacked = didComm.generateEncryptedDIDCommMessage(
            wallet,
            pairwiseContact,
            "2021-03-09T12:32:10Z", // todo...
            message,
            context
        )

        val messageSelfPacked = didComm.generateEncryptedDIDCommMessage(
            wallet,
            pairwiseContact.copy(
                metadata = pairwiseContact.metadata.copy(
                    theirVerkey = pairwiseContact.metadata.myVerkey,
                    theirRoutingKeys = emptyList()
                )
            ),
            "2021-03-09T12:32:10Z", // todo...
            message,
            context
        )

        launch {
            if (relay.sendDataToEndpoint(messagePacked, pairwiseContact.metadata.theirEndpoint)) {
                val didCommMessage =
                    DIDCommMessage(
                        "2021-03-09T12:32:10Z", // TODO
                        message,
                        id = UUID.randomUUID().toString()
                    )
                messageList.add(MessageDisplayModel(didCommMessage, isSender = true))
                output.updateMessages(messageList)
                relay.storeMessage(pairwiseContact.myDid, messageSelfPacked)
            } else {
                // failed
            }
        }
    }

    override fun chatInfoRequested() {
        if (chatInfoShowing) {
            chatInfoShowing = false
            output.updateChatInfoState(showing = false, data = "")
        } else {
            chatInfoShowing = true
            val dataString = """
                myDid: ${pairwiseContact.myDid}
                myVerkey: ${pairwiseContact.metadata.myVerkey}
                
                theirDid: ${pairwiseContact.theirDid}
                theirVerkey: ${pairwiseContact.metadata.theirVerkey}
                
                theirEndpoint: ${pairwiseContact.metadata.theirEndpoint}
                theirRoutingKeys: ${pairwiseContact.metadata.theirRoutingKeys ?: emptyList()}
            """.trimIndent()
            output.updateChatInfoState(showing = true, data = dataString)
        }
    }

    // endregion

    // region interactor inputs


    // endregion
}
