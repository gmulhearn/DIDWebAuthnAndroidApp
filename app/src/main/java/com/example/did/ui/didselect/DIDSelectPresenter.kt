package com.example.did.ui.didselect

import android.os.Bundle
import com.example.did.common.ObjectDelegate
import javax.inject.Inject

/**
 * DIDSelect VIPER Presenter Implementation
 */
class DIDSelectPresenter @Inject constructor(
        private val interactor: DIDSelectContract.InteractorInput,
        private val router: DIDSelectContract.Router
) : DIDSelectContract.Presenter, DIDSelectContract.InteractorOutput {

    internal val viewDelegate = ObjectDelegate<DIDSelectContract.View>()
    internal val view by viewDelegate

    // region viper lifecycle

    override fun attachView(view: DIDSelectContract.View) {
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

    override fun genDIDClicked() {
        interactor.generateDID()
    }

    override fun didTabClicked(did: DIDSelectModels.DidDisplayModel, tabClicked: String) {
        interactor.didTabClicked(did, tabClicked)
    }

    override fun seedSetAttempt(seedText: String) {
        interactor.attemptToSetSeed(seedText)
    }

    // endregion

    // region view event handlers

    // TODO Add view event handlers

    // endregion

    // region interactor output

    override fun loadDataResult() {
        // TODO handle result
    }

    override fun didGenerated(dids: MutableList<DIDSelectModels.DidDisplayModel>) {
        view.updateDidList(dids)
    }

    override fun generationError() {
        view.onGenerationError()
    }

    override fun seedWordsSet(seedWords: String) {
        view.onSeedWordSet(seedWords)
    }

    override fun walletFinishedLoading() {
        view.onWalletLoaded()
    }

    // TODO Add interactor outputs

    // endregion

}
