package com.example.did.ui.didselect

import android.os.Bundle
import com.example.did.common.MSCoroutineScope
import com.example.did.common.ObjectDelegate
import com.example.did.common.di.qualifier.WalletInformation
import com.example.did.data.DidInfo
import com.example.did.data.WalletInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hyperledger.indy.sdk.did.Did
import org.hyperledger.indy.sdk.wallet.Wallet
import javax.inject.Inject

/**
 * DIDSelect VIPER Interactor Implementation
 */
class DIDSelectInteractor @Inject constructor(
    internal val coroutineScope: MSCoroutineScope,
    @WalletInformation internal val walletInfo: WalletInfo
) : DIDSelectContract.InteractorInput, CoroutineScope by coroutineScope {
    
    internal val outputDelegate = ObjectDelegate<DIDSelectContract.InteractorOutput>()
    internal val output by outputDelegate

    private var wallet: Wallet? = null
    private var dids: MutableList<DidInfo> = mutableListOf()

    
    // region viper lifecycle

    override fun attachOutput(output: DIDSelectContract.InteractorOutput) {
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

    override fun generateDID() {
        var exception = false
        if (dids.size >= 5) {
            output.generationError()
            return
        }
        launch {
            try {
                val did = withContext(Dispatchers.IO) {
                    if (wallet == null) {
                        openWallet()
                    }

                    Did.createAndStoreMyDid(wallet!!, "{}").get()
                }
                dids.add(DidInfo(did.did, did.verkey))
            } catch (e: Exception) {
                exception = true
                println(e)
            }
            if (exception) {
                output.generationError()
            } else {
                output.didGenerated(dids.map { transformDidInfoToModel(it) }
                    .toMutableList())
            }
        }
    }

    private fun transformDidInfoToModel(didInfo: DidInfo): DIDSelectModels.DidDisplayModel {
        return DIDSelectModels.DidDisplayModel(didInfo.did, didInfo.verkey)
    }

    private fun openWallet() {
        wallet = Wallet.openWallet(walletInfo.config, walletInfo.credentials).get()
    }

    // endregion
    
    // region interactor inputs


    // endregion
}
