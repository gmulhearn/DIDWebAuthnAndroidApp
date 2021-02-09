package com.example.did.ui.dids

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import com.example.did.R
import com.example.did.data.DidInfo

class DIDAdapter(private val onClick: (DIDsModels.DidDisplayModel) -> Unit) :
    ListAdapter<DIDsModels.DidDisplayModel, DIDAdapter.DIDViewHolder>(
        FlowerDiffCallback
    ) {

    /* ViewHolder for Flower, takes in the inflated view and the onClick behavior. */
    class DIDViewHolder(itemView: View, val onClick: (DIDsModels.DidDisplayModel) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val didTextView: TextView = itemView.findViewById(R.id.did_text)
        private val verkeyTextView: TextView = itemView.findViewById(R.id.verkey_text)
        private var currentDid: DIDsModels.DidDisplayModel? = null

        init {
            itemView.setOnClickListener {
                currentDid?.let {
                    onClick(it)
                }
            }
        }

        /* Bind flower name and image. */
        fun bind(did: DIDsModels.DidDisplayModel) {
            currentDid = did

            didTextView.text = did.did
            verkeyTextView.text = did.did
        }
    }

    /* Creates and inflates view and return FlowerViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DIDViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.did_item, parent, false)
        return DIDViewHolder(
            view,
            onClick
        )
    }

    /* Gets current flower and uses it to bind view. */
    override fun onBindViewHolder(holder: DIDViewHolder, position: Int) {
        val flower = getItem(position)
        holder.bind(flower)

    }
}

object FlowerDiffCallback : DiffUtil.ItemCallback<DIDsModels.DidDisplayModel>() {
    override fun areItemsTheSame(oldItem: DIDsModels.DidDisplayModel, newItem: DIDsModels.DidDisplayModel): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: DIDsModels.DidDisplayModel, newItem: DIDsModels.DidDisplayModel): Boolean {
        return oldItem.did == newItem.did
    }
}