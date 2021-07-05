package com.gmulhearn.didwebauthn.ui.browser

import android.os.Bundle
import com.gmulhearn.didwebauthn.common.ObjectDelegate
import javax.inject.Inject

/**
 * Browser VIPER Presenter Implementation
 */
class BrowserPresenter @Inject constructor(
        private val interactor: BrowserContract.InteractorInput,
        private val router: BrowserContract.Router
) : BrowserContract.Presenter, BrowserContract.InteractorOutput {

    internal val viewDelegate = ObjectDelegate<BrowserContract.View>()
    internal val view by viewDelegate

    // region viper lifecycle

    override fun attachView(view: BrowserContract.View) {
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

    override fun querySubmitted(query: String) {
        // interactor.resolveQuery(query) TODO
        if (query.commonPrefixWith("http") == "http") {
            view.loadUrl(query)
        } else {
            view.loadUrl("https://$query")
        }
    }

    // endregion

    // region view event handlers

    // TODO Add view event handlers

    // endregion

    // region interactor output

    override fun loadDataResult() {
        // TODO handle result
    }

    // TODO Add interactor outputs

    // endregion

}
