package com.example.did.ui.didcomm.contactselect

import android.os.Bundle
import com.example.did.common.MSCoroutineScope
import com.example.did.common.ObjectDelegate
import com.example.did.common.WalletProvider
import com.example.did.common.di.qualifier.DidInformation
import com.example.did.data.DidInfo
import com.example.did.data.PairwiseContact
import com.example.did.data.PairwiseData
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hyperledger.indy.sdk.did.Did
import org.hyperledger.indy.sdk.pairwise.Pairwise
import org.hyperledger.indy.sdk.wallet.Wallet
import java.lang.reflect.Type
import javax.inject.Inject

/**
 * ContactSelect VIPER Interactor Implementation
 */


data class PairwiseContactList(
    val list: List<PairwiseContact>
)

class ContactSelectInteractor @Inject constructor(
    internal val coroutineScope: MSCoroutineScope,
    internal val router: ContactSelectRouter,
    private val walletProvider: WalletProvider
) : ContactSelectContract.InteractorInput, CoroutineScope by coroutineScope {

    internal val outputDelegate = ObjectDelegate<ContactSelectContract.InteractorOutput>()
    internal val output by outputDelegate

    private lateinit var wallet: Wallet

    private var seedWords: List<String> = listOf()
    private var seedHex: String = "TODOTODOTODOTODOTODOTODOTODOTODOTODOTODOTODOTODOTODOTODOTODOTODO" // TODO


    // region viper lifecycle

    override fun attachOutput(output: ContactSelectContract.InteractorOutput) {
        outputDelegate.attach(output)
    }

    override fun detachOutput() {
        coroutineScope.cancelJobs()
        outputDelegate.detach()
    }

    override fun loadData(savedState: Bundle?) {
        getPairwiseContacts()
        loadWallet()
    }

    private fun loadWallet() {
        launch {
            wallet = withContext(Dispatchers.IO) {
                walletProvider.getWallet()
            }
            output.walletFinishedLoading()
        }
    }

    override fun savePendingState(outState: Bundle) {
    }

    private fun getPairwiseContacts() {
        launch {
            val myContacts: List<PairwiseContact> = withContext(Dispatchers.IO) {
                val listString = Pairwise.listPairwise(wallet).get()
                val formattedListString =
                    """{"list": $listString"""
                        .replace("""\""", "")
                        .replace("\"[", "[")
                        .replace("]\"", "")
                        .replace("\"{", "{")
                        .replace("}\"", "}") + """}"""
                println(formattedListString)
                val listContacts =
                    Gson().fromJson(formattedListString, PairwiseContactList::class.java)
                println(listContacts)

                listContacts.list
                    .filter {
                        it.metadata.userDeleted == false
                    }
            }
            output.updateContactList(myContacts)
        }
    }

    override fun toAddContact() {
        // TODO UX Loading
        launch {
            val newDidInfo = withContext(Dispatchers.IO) {
                generateDID()
            }
            router.toAddContact(newDidInfo)
        }

    }

    override fun toChat(pairwiseContact: PairwiseContact) {
        println("CHAT CLICKED: $pairwiseContact")
        router.toChat(pairwiseContact)
    }

    override fun deleteContact(pairwiseContact: PairwiseContact) {
        launch {
            val metadata = Gson().toJson(
                PairwiseData(
                    pairwiseContact.metadata.label,
                    pairwiseContact.metadata.theirEndpoint,
                    pairwiseContact.metadata.theirVerkey,
                    pairwiseContact.metadata.myVerkey,
                    pairwiseContact.metadata.theirRoutingKeys,
                    userDeleted = true
                )
            ).replace("""\u003d""", "=")
            Pairwise.setPairwiseMetadata(wallet, pairwiseContact.theirDid, metadata).get()

            getPairwiseContacts()
        }
    }

    // endregion

    // region interactor inputs

    private suspend fun generateDID(): DidInfo {
        var exception = false
        val did = withContext(Dispatchers.IO) {

            Did.createAndStoreMyDid(
                wallet,
                "{}" // "{\"seed\": \"${(dids.size.toString() + seedHex).subSequence(0, 32)}\"}" TODO
            ).get()
        }
        return DidInfo(did.did, did.verkey)
    }


    // endregion
}
