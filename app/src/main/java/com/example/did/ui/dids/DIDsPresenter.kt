package com.example.did.ui.dids

import android.os.Bundle
import com.example.did.common.ObjectDelegate
import javax.inject.Inject

/**
 * DIDs VIPER Presenter Implementation
 */
class DIDsPresenter @Inject constructor(
        private val interactor: DIDsContract.InteractorInput,
        private val router: DIDsContract.Router
) : DIDsContract.Presenter, DIDsContract.InteractorOutput {

    internal val viewDelegate = ObjectDelegate<DIDsContract.View>()
    internal val view by viewDelegate

    // region viper lifecycle

    override fun attachView(view: DIDsContract.View) {
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

    // endregion

    // region view event handlers

    // TODO Add view event handlers

    // endregion

    // region interactor output

    override fun loadDataResult() {
        // TODO handle result
    }

    override fun didGenerated(dids: MutableList<DIDsModels.DidDisplayModel>) {
        view.updateDidList(dids)
    }

    override fun generationError() {
        view.onGenerationError()
    }

    // TODO Add interactor outputs

    // endregion

}
