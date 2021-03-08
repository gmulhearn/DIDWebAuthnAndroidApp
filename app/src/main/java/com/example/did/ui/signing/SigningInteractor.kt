package com.example.did.ui.signing


import android.os.Bundle
import com.example.did.common.MSCoroutineScope
import com.example.did.common.ObjectDelegate
import com.example.did.common.WalletProvider
import com.example.did.common.di.qualifier.DidInformation
import com.example.did.common.di.qualifier.WalletInformation
import com.example.did.data.DidInfo
import com.example.did.data.WalletInfo
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
    @DidInformation internal val didInfo: DidInfo,
    private val walletProvider: WalletProvider
) : SigningContract.InteractorInput, CoroutineScope by coroutineScope {

    internal val outputDelegate = ObjectDelegate<SigningContract.InteractorOutput>()
    internal val output by outputDelegate

    internal var wallet: Wallet = walletProvider.getWallet()

    // region viper lifecycle

    override fun attachOutput(output: SigningContract.InteractorOutput) {
        outputDelegate.attach(output)
    }

    override fun detachOutput() {
        coroutineScope.cancelJobs()
        outputDelegate.detach()
    }

    override fun loadData(savedState: Bundle?) {

    }

    override fun savePendingState(outState: Bundle) {
        // TODO save interactor state to bundle and output success if required
    }

    override fun signText(text: String) {
        launch {
            val signature = withContext(Dispatchers.IO) {

                Crypto.cryptoSign(wallet, didInfo.verkey, text.toByteArray(Charsets.UTF_8))
                        .get()
            }
            output.signTextResult(signature.toString(Charsets.UTF_8))
        }
    }

    // endregion

    // region interactor inputs


    // endregion
}
