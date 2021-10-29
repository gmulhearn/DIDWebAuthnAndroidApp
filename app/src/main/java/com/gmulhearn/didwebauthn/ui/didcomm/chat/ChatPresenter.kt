package com.gmulhearn.didwebauthn.ui.didcomm.chat

import android.os.Bundle
import com.gmulhearn.didwebauthn.common.ObjectDelegate
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

    override fun sendClicked(textField: String) {
        interactor.sendMessage(textField)
    }

    override fun infoButtonClicked() {
        interactor.chatInfoRequested()
    }

    // endregion

    // region view event handlers

    // endregion

    // region interactor output

    override fun loadDataResult() {
    }

    override fun updateMessages(messageList: MutableList<MessageDisplayModel>) {
        view.updateMessagesList(messageList)
    }

    override fun updateChatInfoState(showing: Boolean, data: String) {
        if (showing) {
            view.showChatInfo(data)
        } else {
            view.hideChatInfo()
        }
    }


    // endregion

}
