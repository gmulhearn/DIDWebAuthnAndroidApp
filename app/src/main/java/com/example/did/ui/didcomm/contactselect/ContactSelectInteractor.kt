package com.example.did.ui.didcomm.contactselect

import android.os.Bundle
import com.example.did.common.MSCoroutineScope
import com.example.did.common.ObjectDelegate
import com.example.did.common.WalletProvider
import com.example.did.common.di.qualifier.DidInformation
import com.example.did.data.DidInfo
import kotlinx.coroutines.CoroutineScope
import org.hyperledger.indy.sdk.wallet.Wallet
import javax.inject.Inject

/**
 * ContactSelect VIPER Interactor Implementation
 */
class ContactSelectInteractor @Inject constructor(
    internal val coroutineScope: MSCoroutineScope,
    @DidInformation internal val didInfo: DidInfo,
    internal val router: ContactSelectRouter,
    private val walletProvider: WalletProvider
) : ContactSelectContract.InteractorInput, CoroutineScope by coroutineScope {

    internal val outputDelegate = ObjectDelegate<ContactSelectContract.InteractorOutput>()
    internal val output by outputDelegate

    internal var wallet: Wallet = walletProvider.getWallet()


    // region viper lifecycle

    override fun attachOutput(output: ContactSelectContract.InteractorOutput) {
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

    override fun toAddContact() {

        router.toAddContact(didInfo)
    }

    // endregion

    // region interactor inputs


    // endregion
}
