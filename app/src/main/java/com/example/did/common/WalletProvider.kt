package com.example.did.common

import org.hyperledger.indy.sdk.wallet.Wallet
import java.lang.Exception
import javax.inject.Inject

interface WalletProvider {
    fun getWallet(): Wallet
}

class DefaultWalletProvider @Inject constructor() : WalletProvider {
    internal var wallet: Wallet? = null

    override fun getWallet(): Wallet {
        val key = "5dd8DLF9GP6V9dEeQeHxsmGnBfaLxZnERrToak8sfCTJ"
        val credentials = "{\"key\":\"$key\"}"
        val config = "{\"id\":\"testID1\"}"

        if (wallet != null) {
            return wallet!!
        }

        // try opening
        try {
            wallet = Wallet.openWallet(config, credentials).get()
        } catch (e: Exception) {
            // create then open
            Wallet.createWallet(config, credentials).get()
            wallet = Wallet.openWallet(config, credentials).get()
        }
        return wallet!!
    }

}