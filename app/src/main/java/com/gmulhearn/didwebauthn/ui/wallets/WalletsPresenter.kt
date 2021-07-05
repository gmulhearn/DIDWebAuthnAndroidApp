package com.gmulhearn.didwebauthn.ui.wallets

import com.gmulhearn.didwebauthn.common.ObjectDelegate
import android.os.Bundle
import javax.inject.Inject

/**
 * Wallets VIPER Presenter Implementation
 */
class WalletsPresenter @Inject constructor(
    private val interactor: WalletsContract.InteractorInput,
    private val router: WalletsContract.Router
) : WalletsContract.Presenter, WalletsContract.InteractorOutput {

    internal val viewDelegate = ObjectDelegate<WalletsContract.View>()
    internal val view by viewDelegate

    // region viper lifecycle

    override fun attachView(view: WalletsContract.View) {
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
        interactor.toWalletDIDSelect(name)
    }


    // endregion

    // region view event handlers

    // TODO Add view event handlers

    // endregion

    // region interactor output

    override fun loadDataResult() {
        // TODO handle result
    }

    override fun walletGenerated(wallets: MutableList<WalletsModels.WalletDisplayModel>) {
        view.updateWalletList(wallets)
    }

    override fun generationError() {
        view.onGenerationError()
    }

    // TODO Add interactor outputs

    // endregion

}
