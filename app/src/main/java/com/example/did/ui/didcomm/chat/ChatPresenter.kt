package com.example.did.ui.didcomm.chat

import android.os.Bundle
import com.example.did.common.ObjectDelegate
import javax.inject.Inject

/**
 * Chat VIPER Presenter Implementation
 */
class ChatPresenter @Inject constructor(
        private val interactor: ChatContract.InteractorInput,
        private val router: ChatContract.Router
) : ChatContract.Presenter, ChatContract.InteractorOutput {

    internal val viewDelegate = ObjectDelegate<ChatContract.View>()
    internal val view by viewDelegate

    // region viper lifecycle

    override fun attachView(view: ChatContract.View) {
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
