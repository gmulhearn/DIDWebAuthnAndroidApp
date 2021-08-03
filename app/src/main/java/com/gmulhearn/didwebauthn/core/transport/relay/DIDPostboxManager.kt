package com.gmulhearn.didwebauthn.core.transport.relay

import com.gmulhearn.didwebauthn.common.WalletProvider
import com.gmulhearn.didwebauthn.data.indy.DIDMetaData
import com.gmulhearn.didwebauthn.data.indy.RelayDIDData
import com.google.gson.Gson
import org.hyperledger.indy.sdk.did.Did
import java.lang.Exception

class DIDPostboxManager(
    private val walletProvider: WalletProvider
) {

    private val wallet = walletProvider.getWallet()

    fun checkDIDPostboxExists(did: String): Boolean {
        return try {
            val metadataStr = Did.getDidMetadata(wallet, did).get()
            val metadata = Gson().fromJson(metadataStr, DIDMetaData::class.java)
            metadata.relayDIDData != null
        } catch (e: Exception) {
            false
        }
    }

    fun getPostboxIDForDID(did: String): String {
        val metadataStr = Did.getDidMetadata(wallet, did).get()
        val metadata = Gson().fromJson(metadataStr, DIDMetaData::class.java)
        return metadata.relayDIDData!!.postboxID
    }

    fun storePostboxIDForDID(postboxID: String, did: String) {
        val metadata = DIDMetaData(
            relayDIDData = RelayDIDData(postboxID)
        )
        val metadataJSON = Gson().toJson(metadata)
        Did.setDidMetadata(wallet, did, metadataJSON).get()
    }
}