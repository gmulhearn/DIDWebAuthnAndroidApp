package com.gmulhearn.didwebauthn.ui.didcomm.contactselect

import android.os.Bundle
import com.gmulhearn.didwebauthn.common.MSCoroutineScope
import com.gmulhearn.didwebauthn.common.ObjectDelegate
import com.gmulhearn.didwebauthn.common.WalletProvider
import com.gmulhearn.didwebauthn.data.DidInfo
import com.gmulhearn.didwebauthn.data.indy.PairwiseContact
import com.gmulhearn.didwebauthn.data.indy.PairwiseData
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hyperledger.indy.sdk.did.Did
import org.hyperledger.indy.sdk.pairwise.Pairwise
import org.hyperledger.indy.sdk.wallet.Wallet
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
