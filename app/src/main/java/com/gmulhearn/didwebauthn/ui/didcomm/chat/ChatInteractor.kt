package com.gmulhearn.didwebauthn.ui.didcomm.chat

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.gmulhearn.didwebauthn.common.MSCoroutineScope
import com.gmulhearn.didwebauthn.common.ObjectDelegate
import com.gmulhearn.didwebauthn.common.WalletProvider
import com.gmulhearn.didwebauthn.data.*
import com.gmulhearn.didwebauthn.protocols.DIDExchange.generateEncryptedDIDCommMessage
import com.gmulhearn.didwebauthn.transport.FirebaseRelay
import com.google.firebase.FirebaseApp
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.hyperledger.indy.sdk.crypto.Crypto
import javax.inject.Inject

/**
 * Chat VIPER Interactor Implementation
 */
class ChatInteractor @Inject constructor(
    internal val coroutineScope: MSCoroutineScope,
    private val pairwiseContact: PairwiseContact,
    private val context: Context,
    private val walletProvider: WalletProvider
) : ChatContract.InteractorInput, CoroutineScope by coroutineScope {

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
        val firebase = FirebaseRelay(FirebaseApp.initializeApp(context)!!)
        val androidId =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        launch {
            firebase.waitForMessage(androidId, ::onMessage)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onMessage(data: Map<String, Any?>) {

        println("onMessage: $data")
        val message = data["message"] as com.google.firebase.firestore.Blob
        val didCommContainer: DIDCommContainer
        try {
            val unencryptedMsg =
                Crypto.unpackMessage(wallet, message.toBytes()).get().toString(Charsets.UTF_8)
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
            messageList.add(MessageDisplayModel(didCommMessage, isSender = false))
            output.updateMessages(messageList)
        } catch (e: java.lang.Exception) {
            println("faile to decode as msg $e")
        }
    }

    override fun savePendingState(outState: Bundle) {
        // TODO save interactor state to bundle and output success if required
    }

    override fun sendMessage(message: String) {
        println("sendMessage Called: $message")
        if (message.isBlank()) {
            return
        }

        val messagePacked = generateEncryptedDIDCommMessage(
            wallet,
            pairwiseContact,
            "2021-03-09T12:32:10Z",
            message,
            context
        )

        launch {
            val firebase = FirebaseRelay(FirebaseApp.initializeApp(context)!!)
            if (firebase.transmitData(messagePacked, pairwiseContact.metadata.theirEndpoint)) {
                val didCommMessage = DIDCommMessage("2021-03-09T12:32:10Z", message, id = "todo")
                messageList.add(MessageDisplayModel(didCommMessage, isSender = true))
                output.updateMessages(messageList)
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
