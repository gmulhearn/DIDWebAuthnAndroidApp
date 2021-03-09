package com.example.did.ui.didcomm.contactselect

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import com.example.did.R
import com.example.did.data.PairwiseContact

class ContactAdapter(private val onClick: (PairwiseContact) -> Unit) :
    ListAdapter<PairwiseContact, ContactAdapter.ContactViewHolder>(
        ContactDiffCallback
    ) {

    /* ViewHolder for Wallet, takes in the inflated view and the onClick behavior. */
    class ContactViewHolder(itemView: View, val onClick: (PairwiseContact) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val contactTextView: TextView = itemView.findViewById(R.id.contactName)
        private var currentContact: PairwiseContact? = null

        init {
            itemView.setOnClickListener {
                currentContact?.let {
                    onClick(it)
                }
            }
        }

        /* Bind flower name and image. */
        fun bind(contact: PairwiseContact) {
            currentContact = contact

            contactTextView.text = contact.metadata.label
        }
    }

    /* Creates and inflates view and return FlowerViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.contact_item, parent, false)
        return ContactViewHolder(
            view,
            onClick
        )
    }

    /* Gets current flower and uses it to bind view. */
    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val flower = getItem(position)
        holder.bind(flower)

    }
}

object ContactDiffCallback : DiffUtil.ItemCallback<PairwiseContact>() {
    override fun areItemsTheSame(oldItem: PairwiseContact, newItem: PairwiseContact): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: PairwiseContact, newItem: PairwiseContact): Boolean {
        return oldItem.theirDid == newItem.theirDid
    }
}