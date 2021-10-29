package com.gmulhearn.didwebauthn.ui.home

import android.os.Bundle
import com.gmulhearn.didwebauthn.common.ObjectDelegate
import com.gmulhearn.didwebauthn.data.DidInfo
import com.gmulhearn.didwebauthn.data.indy.PairwiseContact
import javax.inject.Inject

/**
 * Home VIPER Presenter Implementation
 */
class HomePresenter @Inject constructor(
    private val interactor: HomeContract.InteractorInput,
    private val router: HomeContract.Router
) : HomeContract.Presenter, HomeContract.InteractorOutput {

    internal val viewDelegate = ObjectDelegate<HomeContract.View>()
    internal val view by viewDelegate

    // region viper lifecycle

    override fun attachView(view: HomeContract.View) {
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
        router.toBrowser()
    }

    override fun walletInfoClicked() {
        router.toWalletInfo()
    }

    override fun externalSessionClicked() {
        router.toExternalSession()
    }

    // endregion

    // region view event handlers

    // endregion

    // region interactor output

    override fun loadDataResult() {
    }

    override fun updateContactList(myContacts: List<PairwiseContact>) {
        view.updateContactList(myContacts)
    }

    override fun walletFinishedLoading() {
        view.onWalletLoaded()
    }

    // endregion

}
