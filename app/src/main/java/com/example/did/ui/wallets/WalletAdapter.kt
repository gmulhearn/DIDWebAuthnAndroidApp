package com.example.did.ui.wallets

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import com.example.did.R

class WalletAdapter(private val onClick: (WalletsModels.WalletDisplayModel) -> Unit) :
    ListAdapter<WalletsModels.WalletDisplayModel, WalletAdapter.WalletViewHolder>(
        FlowerDiffCallback
    ) {

    /* ViewHolder for Flower, takes in the inflated view and the onClick behavior. */
    class WalletViewHolder(itemView: View, val onClick: (WalletsModels.WalletDisplayModel) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val flowerTextView: TextView = itemView.findViewById(R.id.walletID_text)
        private val flowerImageView: ImageView = itemView.findViewById(R.id.wallet_image)
        private var currentFlower: WalletsModels.WalletDisplayModel? = null

        init {
            itemView.setOnClickListener {
                currentFlower?.let {
                    onClick(it)
                }
            }
        }

        /* Bind flower name and image. */
        fun bind(wallet: WalletsModels.WalletDisplayModel) {
            currentFlower = wallet

            flowerTextView.text = wallet.walletID
        }
    }

    /* Creates and inflates view and return FlowerViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.wallet_item, parent, false)
        return WalletViewHolder(
            view,
            onClick
        )
    }

    /* Gets current flower and uses it to bind view. */
    override fun onBindViewHolder(holder: WalletViewHolder, position: Int) {
        val flower = getItem(position)
        holder.bind(flower)

    }
}

object FlowerDiffCallback : DiffUtil.ItemCallback<WalletsModels.WalletDisplayModel>() {
    override fun areItemsTheSame(oldItem: WalletsModels.WalletDisplayModel, newItem: WalletsModels.WalletDisplayModel): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: WalletsModels.WalletDisplayModel, newItem: WalletsModels.WalletDisplayModel): Boolean {
        return oldItem.walletID == newItem.walletID
    }
}