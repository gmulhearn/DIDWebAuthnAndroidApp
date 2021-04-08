package com.example.did.ui.browser

import android.os.Bundle
import com.example.did.common.MSCoroutineScope
import com.example.did.common.ObjectDelegate
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

/**
 * Browser VIPER Interactor Implementation
 */
class BrowserInteractor @Inject constructor(
    internal val coroutineScope: MSCoroutineScope
) : BrowserContract.InteractorInput, CoroutineScope by coroutineScope {
    
    internal val outputDelegate = ObjectDelegate<BrowserContract.InteractorOutput>()
    internal val output by outputDelegate
    
    // region viper lifecycle

    override fun attachOutput(output: BrowserContract.InteractorOutput) {
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
