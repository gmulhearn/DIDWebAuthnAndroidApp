package com.example.did.ui.wallets

import android.content.Context
import android.os.Build
import com.example.did.common.MSCoroutineScope
import com.example.did.common.ObjectDelegate
import android.os.Bundle
import android.os.Parcelable
import android.system.Os
import androidx.annotation.RequiresApi
import androidx.navigation.NavType
import com.example.did.data.DidInfo
import com.example.did.data.WalletInfo
import kotlinx.coroutines.*
import org.hyperledger.indy.sdk.did.Did
import org.hyperledger.indy.sdk.wallet.Wallet
import javax.inject.Inject
import com.example.did.ui.wallets.WalletsModels.*
import kotlinx.android.parcel.Parcelize
import org.json.JSONObject
import java.security.SecureRandom

/**
 * Wallets VIPER Interactor Implementation
 */
class WalletsInteractor @Inject constructor(
    internal val coroutineScope: MSCoroutineScope,
    internal val context: Context,
    private val router: WalletsContract.Router
) : WalletsContract.InteractorInput, CoroutineScope by coroutineScope {

    companion object {
        // bundle key for saved wallets
        internal const val WALLETS_OUTSTATE = "WALLETS_OUTSTATE"
    }

    @Parcelize
    data class ParcelableWalletInfoList(val wallets: MutableList<WalletInfo>) : Parcelable

    internal val outputDelegate = ObjectDelegate<WalletsContract.InteractorOutput>()
    internal val output by outputDelegate

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
        (savedState?.getParcelable<ParcelableWalletInfoList>(WALLETS_OUTSTATE))?.wallets?.let {
            wallets = it
        }
    }

    override fun savePendingState(outState: Bundle) {
        outState.putParcelable(WALLETS_OUTSTATE, ParcelableWalletInfoList(wallets))
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

    @RequiresApi(Build.VERSION_CODES.O)
    internal fun generateSeed(): ByteArray? {
        val ent = SecureRandom.getInstanceStrong().generateSeed(16)
        return ent
    }

    // endregion

    // region interactor inputs


    // endregion
}
