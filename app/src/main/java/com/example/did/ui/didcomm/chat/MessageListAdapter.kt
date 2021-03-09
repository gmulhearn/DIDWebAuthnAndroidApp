package com.example.did.ui.didcomm.chat

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.DiffUtil
import com.example.did.R
import com.example.did.data.PairwiseContact

class MessageListAdapter(private val onClick: (PairwiseContact) -> Unit) :
    ListAdapter<MessageDisplayModel, MessageListAdapter.MessageViewHolder>(
        MessageDiffCallback
    ) {

    /* ViewHolder for Wallet, takes in the inflated view and the onClick behavior. */
    class MessageViewHolder(itemView: View, val onClick: (PairwiseContact) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.message_text)
        private val messageItem: ConstraintLayout = itemView.findViewById(R.id.messageItem)
        private var currentMessage: MessageDisplayModel? = null

        init {
            itemView.setOnClickListener {
                currentMessage?.let {
                    // onClick(it)
                }
            }
        }

        /* Bind flower name and image. */
        fun bind(message: MessageDisplayModel) {
            currentMessage = message

            messageTextView.text = message.didCommMessage.content
            if (message.isSender) {
                messageItem.setBackgroundColor(itemView.context.resources.getColor(R.color.purple_200))
            }
        }
    }

    /* Creates and inflates view and return FlowerViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.message_item, parent, false)
        return MessageViewHolder(
            view,
            onClick
        )
    }

    /* Gets current flower and uses it to bind view. */
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val flower = getItem(position)
        holder.bind(flower)

    }
}

object MessageDiffCallback : DiffUtil.ItemCallback<MessageDisplayModel>() {
    override fun areItemsTheSame(oldItem: MessageDisplayModel, newItem: MessageDisplayModel): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: MessageDisplayModel, newItem: MessageDisplayModel): Boolean {
        return oldItem.didCommMessage.content == newItem.didCommMessage.content
    }
}