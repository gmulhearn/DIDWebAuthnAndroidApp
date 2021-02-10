package com.example.did.ui.wallets

import android.content.Context
import com.example.did.common.MSCoroutineScope
import com.example.did.common.ObjectDelegate
import android.os.Bundle
import com.example.did.data.DidInfo
import com.example.did.data.WalletInfo
import kotlinx.coroutines.*
import org.hyperledger.indy.sdk.did.Did
import org.hyperledger.indy.sdk.wallet.Wallet
import javax.inject.Inject
import com.example.did.ui.wallets.WalletsModels.*
import org.json.JSONObject

/**
 * Wallets VIPER Interactor Implementation
 */
class WalletsInteractor @Inject constructor(
    internal val coroutineScope: MSCoroutineScope,
    internal val context: Context,
    private val router: WalletsContract.Router
) : WalletsContract.InteractorInput, CoroutineScope by coroutineScope {

    internal val outputDelegate = ObjectDelegate<WalletsContract.InteractorOutput>()
    internal val output by outputDelegate

    private var wallet: Wallet? = null
    private var didInfo: DidInfo? = null
    private var walletInfo: WalletInfo? = null
    private var wallets: MutableList<WalletInfo> = mutableListOf()

    // region viper lifecycle

    override fun attachOutput(output: WalletsContract.InteractorOutput) {
        outputDelegate.attach(output)
    }

    override fun detachOutput() {
        coroutineScope.cancelJobs()
        outputDelegate.detach()
    }

    override fun loadData(savedState: Bundle?) {
        // TODO implement this. Call output with results of a data load or load existing state
    }

    override fun savePendingState(outState: Bundle) {
        // TODO save interactor state to bundle and output success if required
    }

    private fun importWallet(): Wallet {

        val key = "5dd8DLF9GP6V9dEeQeHxsmGnBfaLxZnERrToak8sfCTJ"
        val credentials = "{\"key\":\"$key\"}"
        val credentials2 = """
            {"key":"$key"}
            """.trimIndent()
        val config = "{\"id\":\"testID1\"}"
        val importJson = "{\"path\":\"/sdcard/Documents/indyWalletTest\", \"key\":\"password\" }"

        Wallet.importWallet(config, credentials, importJson).get()

        val wallet = Wallet.openWallet(config, credentials).get()

        return wallet
    }

    override fun generateWallet(name: String) {
        var exception = false
        if (wallets.size >= 3) {
            output.generationError()
            return
        }
        launch {
            try {
                withContext(Dispatchers.IO) {
                    val key = Wallet.generateWalletKey(null).get()

                    val credentials = "{\"key\":\"$key\"}"
                    val config = "{\"id\":\"$name\"}"

                    Wallet.createWallet(config, credentials).get()

                    wallets.add(WalletInfo(config, credentials))
                }
            } catch (e: Exception) {
                exception = true
                println(e)
            }
            if (exception) {
                output.generationError()
            } else {
                output.walletGenerated(wallets.map { transformWalletInfoToModel(it) }
                    .toMutableList())
            }
        }
    }

    private fun transformWalletInfoToModel(walletInfo: WalletInfo): WalletDisplayModel {
        val walletID = JSONObject(walletInfo.config).getString("id")
        return WalletDisplayModel(walletID)
    }

    override fun toWalletDIDSelect(walletName: String) {
        router.toDIDSelect(walletNameToInfo(walletName))
    }

    private fun walletNameToInfo(walletName: String): WalletInfo {
        return wallets.first { JSONObject(it.config).getString("id") == walletName }
    }

    // endregion

    // region interactor inputs


    // endregion
}
