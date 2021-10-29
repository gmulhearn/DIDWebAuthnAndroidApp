package com.gmulhearn.didwebauthn.ui.retired.wallets

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import com.gmulhearn.didwebauthn.R

class WalletAdapter(private val onClick: (WalletsModels.WalletDisplayModel) -> Unit) :
    ListAdapter<WalletsModels.WalletDisplayModel, WalletAdapter.WalletViewHolder>(
        FlowerDiffCallback
    ) {

    /* ViewHolder for Wallet, takes in the inflated view and the onClick behavior. */
    class WalletViewHolder(itemView: View, val onClick: (WalletsModels.WalletDisplayModel) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val walletTextView: TextView = itemView.findViewById(R.id.wallet_text)
        private var currentWallet: WalletsModels.WalletDisplayModel? = null

        init {
            itemView.setOnClickListener {
                currentWallet?.let {
                    onClick(it)
                }
            }
        }

        /* Bind flower name and image. */
        fun bind(wallet: WalletsModels.WalletDisplayModel) {
            currentWallet = wallet

            walletTextView.text = wallet.walletID
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