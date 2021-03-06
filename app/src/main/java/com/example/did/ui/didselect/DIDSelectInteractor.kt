package com.example.did.ui.didselect

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.example.did.common.MSCoroutineScope
import com.example.did.common.ObjectDelegate
import com.example.did.common.di.qualifier.WalletInformation
import com.example.did.data.DidInfo
import com.example.did.data.WalletInfo
import com.example.did.protocols.BIP0039.generateMnemonic
import com.example.did.protocols.BIP0039.generateSeed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hyperledger.indy.sdk.did.Did
import org.hyperledger.indy.sdk.wallet.Wallet
import java.util.*
import javax.inject.Inject

/**
 * DIDSelect VIPER Interactor Implementation
 */
class DIDSelectInteractor @Inject constructor(
    internal val coroutineScope: MSCoroutineScope,
    @WalletInformation internal var walletInfo: WalletInfo,
    private val router: DIDSelectRouter,
    private val context: Context
) : DIDSelectContract.InteractorInput, CoroutineScope by coroutineScope {

    companion object {
        internal const val WALLET_INFO = "WALLET_INFO"
    }
    
    internal val outputDelegate = ObjectDelegate<DIDSelectContract.InteractorOutput>()
    internal val output by outputDelegate

    private var wallet: Wallet? = null
    private var dids: MutableList<DidInfo> = mutableListOf()

    private var seedWords: List<String> = listOf()
    private var seedHex: String = ""

    
    // region viper lifecycle

    override fun attachOutput(output: DIDSelectContract.InteractorOutput) {
        outputDelegate.attach(output)
    }
    
    override fun detachOutput() {
        coroutineScope.cancelJobs()
        outputDelegate.detach()
    }

    @ExperimentalUnsignedTypes
    @RequiresApi(Build.VERSION_CODES.O)
    override fun loadData(savedState: Bundle?) {
        println("loading data")
        savedState?.getParcelable<WalletInfo>(WALLET_INFO)?.let {
            walletInfo = it
        }
        openWallet()
        generateSeedWords()
    }

    override fun savePendingState(outState: Bundle) {
        println("saving data")
        outState.putParcelable(WALLET_INFO, walletInfo)
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

                    Did.createAndStoreMyDid(wallet!!, "{\"seed\": \"${(dids.size.toString() + seedHex).subSequence(0, 32)}\"}").get()
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

    override fun didTabClicked(did: DIDSelectModels.DidDisplayModel, tabClicked: String) {
        val didInfo = getDidInfoFromModel(did)

        if (wallet == null) {
            openWallet()
        }

        checkNotNull(wallet)

        wallet!!.closeWallet().get()
        wallet = null

        when (tabClicked) {
            "sign" -> router.toSigning(didInfo, walletInfo)
            "comm" -> router.toContacts(didInfo, walletInfo)
            "browser" -> {}
            else -> {}
        }
    }

    @ExperimentalUnsignedTypes
    @RequiresApi(Build.VERSION_CODES.O)
    override fun attemptToSetSeed(seedText: String) {
        val newSeedWords = seedText.split(" ")
        try { // TODO fix this to actually catch
            seedWords = newSeedWords
            seedHex = Base64.getEncoder().encodeToString(
                generateSeed(
                    seedWords
                )
            ).substring(0, 32)
        } catch (e: java.lang.Exception) {

        }
        output.seedWordsSet(seedWords.joinToString(" "))
    }

    private fun transformDidInfoToModel(didInfo: DidInfo): DIDSelectModels.DidDisplayModel {
        return DIDSelectModels.DidDisplayModel(didInfo.did, didInfo.verkey)
    }

    private fun getDidInfoFromModel(didModel: DIDSelectModels.DidDisplayModel): DidInfo {
        return dids.first { it.did == didModel.did }
    }

    private fun openWallet() {
        println("Opening Wallet")
        try {
            wallet = Wallet.openWallet(walletInfo.config, walletInfo.credentials).get()
        } catch (e: java.lang.Exception) {
            println(e)
        }
    }

    @ExperimentalUnsignedTypes
    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateSeedWords() {
        seedWords = generateMnemonic(128, context)
        seedHex = Base64.getEncoder().encodeToString(
            generateSeed(
                seedWords
            )
        ).substring(0, 32)
        output.seedWordsSet(seedWords.joinToString(" "))
    }

    // endregion
    
    // region interactor inputs


    // endregion
}
