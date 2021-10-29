package com.gmulhearn.didwebauthn.ui.browser

import android.os.Bundle
import com.gmulhearn.didwebauthn.common.MSCoroutineScope
import com.gmulhearn.didwebauthn.common.ObjectDelegate
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
    }

    override fun savePendingState(outState: Bundle) {
    }

    // endregion
    
    // region interactor inputs


    // endregion
}
