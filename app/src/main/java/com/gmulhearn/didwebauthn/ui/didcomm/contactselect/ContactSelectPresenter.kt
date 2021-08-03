package com.gmulhearn.didwebauthn.ui.didcomm.contactselect

import android.os.Bundle
import com.gmulhearn.didwebauthn.common.ObjectDelegate
import com.gmulhearn.didwebauthn.data.DidInfo
import com.gmulhearn.didwebauthn.data.indy.PairwiseContact
import javax.inject.Inject

/**
 * ContactSelect VIPER Presenter Implementation
 */
class ContactSelectPresenter @Inject constructor(
        private val interactor: ContactSelectContract.InteractorInput,
        private val router: ContactSelectContract.Router
) : ContactSelectContract.Presenter, ContactSelectContract.InteractorOutput {

    internal val viewDelegate = ObjectDelegate<ContactSelectContract.View>()
    internal val view by viewDelegate

    // region viper lifecycle

    override fun attachView(view: ContactSelectContract.View) {
        viewDelegate.attach(view)
        interactor.attachOutput(this)
    }

    override fun detachView() {
        interactor.detachOutput()
        viewDelegate.detach()
    }

    override fun viewLoaded(savedState: Bundle?) {
        interactor.loadData(savedState)
    }

    override fun saveState(outState: Bundle) {
        interactor.savePendingState(outState)
    }

    override fun addContactClicked() {
        interactor.toAddContact()
    }

    override fun contactClicked(pairwiseContact: PairwiseContact) {
        interactor.toChat(pairwiseContact)
    }

    override fun deleteClicked(pairwiseContact: PairwiseContact) {
        interactor.deleteContact(pairwiseContact)
    }

    override fun browserClicked() {
        router.toBrowser(DidInfo("todo", "todo")) // TODO
    }

    override fun walletInfoClicked() {
        router.toWalletInfo()
    }

    override fun externalAuthClicked() {
        //router.toExternalAuth()
        router.toExternalSession()
    }

    // endregion

    // region view event handlers

    // TODO Add view event handlers

    // endregion

    // region interactor output

    override fun loadDataResult() {
        // TODO handle result
    }

    override fun updateContactList(myContacts: List<PairwiseContact>) {
        view.updateContactList(myContacts)
    }

    override fun walletFinishedLoading() {
        view.onWalletLoaded()
    }


    // TODO Add interactor outputs

    // endregion

}