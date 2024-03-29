package com.gmulhearn.didwebauthn.ui.walletInfo

import android.os.Bundle
import com.gmulhearn.didwebauthn.common.ObjectDelegate
import javax.inject.Inject

/**
 * WalletInfo VIPER Presenter Implementation
 */
class WalletInfoPresenter @Inject constructor(
    private val interactor: WalletInfoContract.InteractorInput,
    private val router: WalletInfoContract.Router
) : WalletInfoContract.Presenter, WalletInfoContract.InteractorOutput {

    internal val viewDelegate = ObjectDelegate<WalletInfoContract.View>()
    internal val view by viewDelegate

    // region viper lifecycle

    override fun attachView(view: WalletInfoContract.View) {
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

    override fun viewDidsClicked(didType: LoadInfoType) {
        interactor.loadInfo(didType)
    }

    override fun restartClicked() {
        interactor.restartWallet()
    }

    // endregion

    // region view event handlers

    // endregion

    // region interactor output

    override fun loadDataResult(walletTitle: String) {
    }

    override fun updateInfo(didsString: String) {
        view.setInfoText(didsString)
    }

    // endregion

}
