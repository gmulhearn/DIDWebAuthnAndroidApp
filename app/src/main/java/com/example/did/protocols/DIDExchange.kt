package com.example.did.protocols

import android.provider.Settings
import com.example.did.data.DidInfo
import com.example.did.data.WalletInfo
import org.hyperledger.indy.sdk.did.Did
import org.hyperledger.indy.sdk.wallet.Wallet

object DIDExchange {
    fun generateInvitation(wallet: Wallet, did: DidInfo) {
        println(did)
        val didKey = Did.keyForLocalDid(wallet, did.did).get()
        val id = Settings.Secure.ANDROID_ID
    }
}