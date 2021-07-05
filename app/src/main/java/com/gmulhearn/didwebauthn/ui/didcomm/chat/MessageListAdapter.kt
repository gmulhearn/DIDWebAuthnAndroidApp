package com.gmulhearn.didwebauthn.ui.didcomm.chat

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import com.gmulhearn.didwebauthn.R
import com.gmulhearn.didwebauthn.data.PairwiseContact

class MessageListAdapter(private val onClick: (PairwiseContact) -> Unit) :
    ListAdapter<MessageDisplayModel, MessageListAdapter.MessageViewHolder>(
        MessageDiffCallback
    ) {

    /* ViewHolder for Wallet, takes in the inflated view and the onClick behavior. */
    class MessageViewHolder(itemView: View, val onClick: (PairwiseContact) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val leftmessageTextView: TextView = itemView.findViewById(R.id.message_left_text)
        private val leftMessageItem: ConstraintLayout = itemView.findViewById(R.id.messageItemLeft)
        private val rightMessageTextView: TextView = itemView.findViewById(R.id.message_right_text)
        private val rightMessageItem: ConstraintLayout = itemView.findViewById(R.id.messageItemRight)
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

            // todo: this could be done better with a single item and constraints
            if (message.isSender) {
                leftMessageItem.visibility = View.GONE
                rightMessageItem.visibility = View.VISIBLE
                rightMessageTextView.text = message.didCommMessage.content
            } else {
                rightMessageItem.visibility = View.GONE
                leftMessageItem.visibility = View.VISIBLE
                leftmessageTextView.text = message.didCommMessage.content
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