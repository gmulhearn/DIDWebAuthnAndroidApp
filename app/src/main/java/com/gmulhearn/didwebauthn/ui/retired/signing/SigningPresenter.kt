package com.gmulhearn.didwebauthn.ui.retired.signing

import android.os.Bundle
import com.gmulhearn.didwebauthn.common.ObjectDelegate
import javax.inject.Inject

/**
 * Signing VIPER Presenter Implementation
 */
class SigningPresenter @Inject constructor(
        private val interactor: SigningContract.InteractorInput,
        private val router: SigningContract.Router
) : SigningContract.Presenter, SigningContract.InteractorOutput {

    internal val viewDelegate = ObjectDelegate<SigningContract.View>()
    internal val view by viewDelegate

    // region viper lifecycle

    override fun attachView(view: SigningContract.View) {
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

    override fun signTextPressed(text: String) {
        interactor.signText(text)
    }

    // endregion

    // region view event handlers

    // endregion

    // region interactor output

    override fun loadDataResult() {
    }

    override fun signTextResult(text: String) {
        view.updateSignedText(text)
    }

    // endregion

}
