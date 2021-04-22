package com.example.did.ui.didcomm.chat

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import dagger.android.support.AndroidSupportInjection
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.did.R
import com.example.did.data.DIDCommMessage
import com.example.did.ui.didcomm.contactselect.ContactAdapter
import kotlinx.android.synthetic.main.chat_info_popup.view.*
import kotlinx.android.synthetic.main.chat_input_item.view.*
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.fragment_contact_select.*
import javax.inject.Inject

/**
 * Chat VIPER Fragment Implementation
 */
class ChatFragment : Fragment(), ChatContract.View {

    @Inject
    internal lateinit var presenter: ChatContract.Presenter

    @VisibleForTesting
    internal val navigationArgs by navArgs<ChatFragmentArgs>()

    internal var adapter =
        MessageListAdapter { _ -> }

    // region viper lifecycle

    override fun onAttach(context: Context) {
        inject()
        super.onAttach(context)
    }

    internal fun inject() {
        AndroidSupportInjection.inject(this)
    }

    // endregion

    // region view setup and state lifecycle

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)

        messageInput.sendButton.setOnClickListener {
            presenter.sendClicked(messageInput.textField.text.toString())
            messageInput.textField.setText("")
        }

        infoButton.setOnClickListener {
            presenter.infoButtonClicked()
        }

        chatTitle.text = navigationArgs.pairwiseContact.metadata.label

        val linearLayoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        linearLayoutManager.stackFromEnd = true
        messageList.layoutManager = linearLayoutManager
        messageList.adapter = adapter

        // Notify Presenter that the View is ready
        presenter.viewLoaded(savedInstanceState)
    }

    override fun onDestroyView() {
        presenter.detachView()
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenter.saveState(outState)
    }

    override fun updateMessagesList(updatedMessageList: MutableList<MessageDisplayModel>) {
        println(updatedMessageList)
        val startMessageList = mutableListOf(MessageDisplayModel(DIDCommMessage("", "start",id="n/a"), false))
        startMessageList.addAll(updatedMessageList)
        adapter.submitList(startMessageList)
        messageList.post {
            messageList.scrollToPosition(updatedMessageList.size)
        }
    }

    override fun showChatInfo(data: String) {
        chatInfoPopup.chatInformation.text = data
        chatInfoPopup.visibility = View.VISIBLE
    }

    override fun hideChatInfo() {
        chatInfoPopup.visibility = View.GONE
    }

    // endregion

    // region View contract

    // TODO Add view contract overrides

    // endregion

}
