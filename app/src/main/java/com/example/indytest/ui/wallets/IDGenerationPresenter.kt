package com.example.indytest.ui.wallets

import com.example.indytest.common.ObjectDelegate
import android.os.Bundle
import javax.inject.Inject

/**
 * IDGeneration VIPER Presenter Implementation
 */
class IDGenerationPresenter @Inject constructor(
        private val interactor: IDGenerationContract.InteractorInput,
        private val router: IDGenerationContract.Router
) : IDGenerationContract.Presenter, IDGenerationContract.InteractorOutput {

    internal val viewDelegate = ObjectDelegate<IDGenerationContract.View>()
    internal val view by viewDelegate

    // region viper lifecycle

    override fun attachView(view: IDGenerationContract.View) {
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

    override fun genWalletPressed(name: String) {
        interactor.generateWallet(name)
    }

    override fun walletClicked(name: String) {
        interactor.toWalletDIDs(name)
    }


    // endregion

    // region view event handlers

    // TODO Add view event handlers

    // endregion

    // region interactor output

    override fun loadDataResult() {
        // TODO handle result
    }

    override fun walletGenerated(wallets: MutableList<IDGenerationModels.WalletDisplayModel>) {
        view.updateWalletList(wallets)
    }

    private fun walletGenerated2(walletID: String) {
        view.updateWalletText(walletID)
    }

    override fun generationError() {
        view.onGenerationError()
    }

    // TODO Add interactor outputs

    // endregion

}
