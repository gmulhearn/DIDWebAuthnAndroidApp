package com.example.indytest.DIDs

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import com.example.indytest.R
import com.example.indytest.Wallets.IDGenerationModels

class DIDAdapter(private val onClick: (IDGenerationModels.DidInfo) -> Unit) :
    ListAdapter<IDGenerationModels.DidInfo, DIDAdapter.DIDViewHolder>(
        FlowerDiffCallback
    ) {

    /* ViewHolder for Flower, takes in the inflated view and the onClick behavior. */
    class DIDViewHolder(itemView: View, val onClick: (IDGenerationModels.DidInfo) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val flowerTextView: TextView = itemView.findViewById(R.id.walletID_text)
        private val flowerImageView: ImageView = itemView.findViewById(R.id.wallet_image)
        private var currentFlower: IDGenerationModels.DidInfo? = null

        init {
            itemView.setOnClickListener {
                currentFlower?.let {
                    onClick(it)
                }
            }
        }

        /* Bind flower name and image. */
        fun bind(did: IDGenerationModels.DidInfo) {
            currentFlower = did

            flowerTextView.text = did.did
        }
    }

    /* Creates and inflates view and return FlowerViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DIDViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.wallet_item, parent, false)
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

object FlowerDiffCallback : DiffUtil.ItemCallback<IDGenerationModels.DidInfo>() {
    override fun areItemsTheSame(oldItem: IDGenerationModels.DidInfo, newItem: IDGenerationModels.DidInfo): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: IDGenerationModels.DidInfo, newItem: IDGenerationModels.DidInfo): Boolean {
        return oldItem.did == newItem.did
    }
}