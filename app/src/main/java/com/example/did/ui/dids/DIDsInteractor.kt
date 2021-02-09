package com.example.did.ui.dids

import android.os.Bundle
import com.example.did.common.MSCoroutineScope
import com.example.did.common.ObjectDelegate
import com.example.did.common.di.qualifier.WalletInformation
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

/**
 * DIDs VIPER Interactor Implementation
 */
class DIDsInteractor @Inject constructor(
    internal val coroutineScope: MSCoroutineScope,
    @WalletInformation internal val walletInfo: com.example.did.data.WalletInfo
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
