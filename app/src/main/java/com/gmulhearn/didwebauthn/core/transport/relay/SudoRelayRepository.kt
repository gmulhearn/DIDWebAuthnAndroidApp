package com.gmulhearn.didwebauthn.core.transport.relay

import android.content.Context
import com.gmulhearn.didwebauthn.common.WalletProvider
import com.sudoplatform.sudodecentralizedidentityrelay.SudoDecentralizedIdentityRelayClient
import com.sudoplatform.sudodecentralizedidentityrelay.subscribeToRelayEvents
import com.sudoplatform.sudodecentralizedidentityrelay.subscription.DecentralizedIdentityRelayEventSubscriber
import com.sudoplatform.sudodecentralizedidentityrelay.types.PostboxDeletionResult
import com.sudoplatform.sudodecentralizedidentityrelay.types.RelayMessage
import com.sudoplatform.sudologging.AndroidUtilsLogDriver
import com.sudoplatform.sudologging.LogLevel
import com.sudoplatform.sudologging.Logger
import java.util.Base64
import java.util.UUID
import javax.inject.Inject

class SudoRelayRepository @Inject constructor(
    context: Context,
    private val walletProvider: WalletProvider
) : RelayRepository {

    companion object {
        private const val BASE_POSTBOX_ENDPOINT =
            "https://ph3oqq1dke.execute-api.us-east-1.amazonaws.com/"
    }

    private val didPostboxManager = DIDPostboxManager(walletProvider)

    private val logger = Logger("didWebAuthnApp", AndroidUtilsLogDriver(LogLevel.DEBUG))

    private val diRelayClient =
        SudoDecentralizedIdentityRelayClient.builder().setContext(context).setLogger(logger).build()

    override suspend fun initializePostbox(did: String) {
        if (!didPostboxManager.checkDIDPostboxExists(did)) {
            val newPostboxID = UUID.randomUUID().toString()
            diRelayClient.createPostbox(newPostboxID)
            didPostboxManager.storePostboxIDForDID(newPostboxID, did)
        }
    }

    override fun getServiceEndpoint(did: String): String {
        val postboxID = didPostboxManager.getPostboxIDForDID(did)

        return "$BASE_POSTBOX_ENDPOINT$postboxID"
    }

    override suspend fun subscribeToMessages(did: String, onReceiveMessage: (ByteArray) -> Unit) {
        val postboxID = didPostboxManager.getPostboxIDForDID(did)

        diRelayClient.subscribeToRelayEvents(postboxID,
            object : DecentralizedIdentityRelayEventSubscriber {
                override fun connectionStatusChanged(state: DecentralizedIdentityRelayEventSubscriber.ConnectionState) {}
                override fun messageIncoming(message: RelayMessage) {
                    println("sudo relay received msg: ${message.cipherText}")
                    onReceiveMessage(
                        Base64.getDecoder().decode(message.cipherText) // todo -hmmm
                    )
                }

                override fun postBoxDeleted(update: PostboxDeletionResult) {}
            }
        )
    }

    override suspend fun getMessages(did: String): List<ByteArray> {
        val postboxID = didPostboxManager.getPostboxIDForDID(did)

        return diRelayClient.getMessages(postboxID).map {
            Base64.getDecoder().decode(it.cipherText)
        }
    }

    override suspend fun storeMessage(did: String, data: ByteArray) {
        val postboxID = didPostboxManager.getPostboxIDForDID(did)
        diRelayClient.storeMessage(
            postboxID,
            Base64.getEncoder().encodeToString(data)
        )
    }

}