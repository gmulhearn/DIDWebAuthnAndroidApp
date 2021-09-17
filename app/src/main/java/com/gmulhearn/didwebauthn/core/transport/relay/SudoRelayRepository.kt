package com.gmulhearn.didwebauthn.core.transport.relay

import android.content.Context
import com.gmulhearn.didwebauthn.common.WalletProvider
import com.sudoplatform.sudoconfigmanager.DefaultSudoConfigManager
import com.sudoplatform.sudodirelay.SudoDIRelayClient
import com.sudoplatform.sudodirelay.subscription.DIRelayEventSubscriber
import com.sudoplatform.sudodirelay.types.PostboxDeletionResult
import com.sudoplatform.sudodirelay.types.RelayMessage
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

    private var basePostboxEndpoint: String

    init {
        val endpoint = DefaultSudoConfigManager(context)
            .getConfigSet("relayService")
            ?.get("httpEndpoint") as String?
        requireNotNull(endpoint)
        basePostboxEndpoint = "$endpoint/"
    }

    private val didPostboxManager = DIDPostboxManager(walletProvider)

    private val logger = Logger("didWebAuthnApp", AndroidUtilsLogDriver(LogLevel.DEBUG))

    private val diRelayClient =
        SudoDIRelayClient.builder().setContext(context).setLogger(logger).build()

    override suspend fun initializePostbox(did: String) {
        if (!didPostboxManager.checkDIDPostboxExists(did)) {
            val newPostboxID = UUID.randomUUID().toString()
            diRelayClient.createPostbox(newPostboxID)
            didPostboxManager.storePostboxIDForDID(newPostboxID, did)
        }
    }

    override fun getServiceEndpoint(did: String): String {
        val postboxID = didPostboxManager.getPostboxIDForDID(did)

        return "$basePostboxEndpoint$postboxID"
    }

    override suspend fun subscribeToMessages(did: String, onReceiveMessage: (ByteArray) -> Unit) {
        val postboxID = didPostboxManager.getPostboxIDForDID(did)

        diRelayClient.subscribeToRelayEvents(postboxID,
            object : DIRelayEventSubscriber {
                override fun connectionStatusChanged(state: DIRelayEventSubscriber.ConnectionState) {}
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