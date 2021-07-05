package com.gmulhearn.didwebauthn.common

import org.hyperledger.indy.sdk.wallet.Wallet
import java.lang.Exception
import javax.inject.Inject

interface WalletProvider {
    fun getWallet(): Wallet

    fun resetWallet()
}

class DefaultWalletProvider @Inject constructor() : WalletProvider {
    internal var wallet: Wallet? = null

    private val key = "5dd8DLF9GP6V9dEeQeHxsmGnBfaLxZnERrToak8sfCTJ"
    private val credentials = "{\"key\":\"$key\"}"
    private val config = "{\"id\":\"testID1\"}"

    override fun getWallet(): Wallet {

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

    override fun resetWallet() {
        getWallet().closeWallet().get()

        Wallet.deleteWallet(config, credentials).get()
    }

}