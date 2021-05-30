package com.example.did.ui.walletInfo

import android.os.Bundle
import com.example.did.common.MSCoroutineScope
import com.example.did.common.ObjectDelegate
import com.example.did.common.WalletProvider
import com.example.did.data.MetadataDID
import com.example.did.data.PairwiseContact
import com.example.did.data.WebAuthnDIDData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hyperledger.indy.sdk.did.Did
import org.hyperledger.indy.sdk.pairwise.Pairwise
import javax.inject.Inject

/**
 * WalletInfo VIPER Interactor Implementation
 */
class WalletInfoInteractor @Inject constructor(
    internal val coroutineScope: MSCoroutineScope,
    internal val walletProvider: WalletProvider
) : WalletInfoContract.InteractorInput, CoroutineScope by coroutineScope {

    internal val outputDelegate = ObjectDelegate<WalletInfoContract.InteractorOutput>()
    internal val output by outputDelegate

    // region viper lifecycle

    override fun attachOutput(output: WalletInfoContract.InteractorOutput) {
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

    override fun loadInfo(type: LoadInfoType) {

        launch {
            val didsString: String = withContext(Dispatchers.IO) {
                val wallet = walletProvider.getWallet()

                if (type == LoadInfoType.PAIRWISEDIDS) {
                    // need to structure/format...
                    val pairwiseStr =
                        Pairwise.listPairwise(walletProvider.getWallet()).get()
                            .replace("""\""", "")
                            .replace("\"[", "[")
                            .replace("]\"", "")
                            .replace("\"{", "{")
                            .replace("}\"", "}")

                    println(pairwiseStr)
                    val pairwiseListType = object : TypeToken<List<PairwiseContact>>() {}.type
                    val pairwiseList =
                        Gson().fromJson<List<PairwiseContact>>(pairwiseStr, pairwiseListType)

                    return@withContext structureDidsToString(pairwiseList)
                }


                val didsString = Did.getListMyDidsWithMeta(wallet).get()

                val metadataDIDType = object : TypeToken<List<MetadataDID>>() {}.type
                val didList = Gson().fromJson<List<MetadataDID>>(didsString, metadataDIDType)

                if (type == LoadInfoType.ALLDIDS) {
                    // return full did list
                    return@withContext structureDidsToString(didList)
                }

                // else return filtered did list
                val filterDidList = didList.filter { metaDid ->
                    var isWebAuthnMeta = false
                    metaDid.metadata?.let {
                        try {
                            Gson().fromJson(it, WebAuthnDIDData::class.java)
                            isWebAuthnMeta = true
                        } catch (e: Exception) {
                        }
                    }
                    isWebAuthnMeta
                }

                return@withContext structureDidsToString(filterDidList)
            }

            output.updateInfo(didsString)
        }
    }

    override fun restartWallet() {
        walletProvider.resetWallet()
    }

    private fun structureDidsToString(dids: List<Any>): String {
        var counter = 0
        return dids.joinToString("\n\n") {
            counter++
            "$counter. $it"
        }
    }

    // endregion

    // region interactor inputs


    // endregion
}
