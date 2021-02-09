package com.example.indytest.DIDs

import android.os.Bundle
import com.example.indytest.Wallets.IDGenerationModels
import com.example.indytest.common.MSCoroutineScope
import com.example.indytest.common.ObjectDelegate
import com.example.indytest.common.di.qualifier.WalletInfo
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

/**
 * DIDs VIPER Interactor Implementation
 */
class DIDsInteractor @Inject constructor(
    internal val coroutineScope: MSCoroutineScope,
    @WalletInfo internal val walletInfo: IDGenerationModels.WalletInfo
) : DIDsContract.InteractorInput, CoroutineScope by coroutineScope {
    
    internal val outputDelegate = ObjectDelegate<DIDsContract.InteractorOutput>()
    internal val output by outputDelegate
    
    // region viper lifecycle

    override fun attachOutput(output: DIDsContract.InteractorOutput) {
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

    // endregion
    
    // region interactor inputs


    // endregion
}
