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
        getPairwiseContacts()
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

                listContacts.list.filter {
                    it.myDid == didInfo.did
                }
            }
            output.updateContactList(myContacts)
        }
    }

    override fun toAddContact() {
        router.toAddContact(didInfo)
    }

    override fun toChat(pairwiseContact: PairwiseContact) {
        println("CHAT CLICKED: $pairwiseContact")
        router.toChat(pairwiseContact)
    }

    // endregion

    // region interactor inputs


    // endregion
}
