package com.example.indytest.Signing


import android.os.Bundle
import com.example.indytest.Wallets.IDGenerationModels
import com.example.indytest.common.MSCoroutineScope
import com.example.indytest.common.ObjectDelegate
import com.example.indytest.common.di.qualifier.DidInfo
import com.example.indytest.common.di.qualifier.WalletInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hyperledger.indy.sdk.crypto.Crypto
import org.hyperledger.indy.sdk.wallet.Wallet
import javax.inject.Inject

/**
 * Signing VIPER Interactor Implementation
 */
class SigningInteractor @Inject constructor(
    internal val coroutineScope: MSCoroutineScope,
    @DidInfo internal val didInfo: IDGenerationModels.DidInfo,
    @WalletInfo internal val walletInfo: IDGenerationModels.WalletInfo
) : SigningContract.InteractorInput, CoroutineScope by coroutineScope {

    internal val outputDelegate = ObjectDelegate<SigningContract.InteractorOutput>()
    internal val output by outputDelegate

    internal var wallet: Wallet? = null

    // region viper lifecycle

    override fun attachOutput(output: SigningContract.InteractorOutput) {
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

    override fun signText(text: String) {
        launch {
            val signature = withContext(Dispatchers.IO) {
                if (wallet == null) {
                    openWallet()
                }


                Crypto.cryptoSign(wallet, didInfo.verkey, text.toByteArray(Charsets.UTF_8))
                        .get()
            }
            output.signTextResult(signature.toList().toString())
        }
    }

    private fun openWallet() {
        wallet = Wallet.openWallet(walletInfo.config, walletInfo.credentials).get()
    }

    // endregion

    // region interactor inputs


    // endregion
}
