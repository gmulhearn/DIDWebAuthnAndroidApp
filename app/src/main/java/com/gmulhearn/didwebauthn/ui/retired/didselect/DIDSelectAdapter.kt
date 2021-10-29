package com.gmulhearn.didwebauthn.ui.retired.didselect

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import com.gmulhearn.didwebauthn.R
import com.google.android.material.tabs.TabLayout

class DIDAdapter(private val onClick: (DIDSelectModels.DidDisplayModel, String) -> Unit) :
    ListAdapter<DIDSelectModels.DidDisplayModel, DIDAdapter.DIDViewHolder>(
        DidDiffCallback
    ) {

    /* ViewHolder for Flower, takes in the inflated view and the onClick behavior. */
    class DIDViewHolder(itemView: View, val onTabClick: (DIDSelectModels.DidDisplayModel, String) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val didTextView: TextView = itemView.findViewById(R.id.did_text)
        private val verkeyTextView: TextView = itemView.findViewById(R.id.verkey_text)
        private val arrow: ImageView = itemView.findViewById(R.id.nextArrow1)

        private val didTabs: TabLayout = itemView.findViewById(R.id.did_tabs)
        private val signTab = didTabs.getTabAt(0)!!
        private val commTab = didTabs.getTabAt(1)!!
        private val browserTab = didTabs.getTabAt(2)!!

        private var currentDid: DIDSelectModels.DidDisplayModel? = null

        init {
            itemView.setOnClickListener {
                currentDid?.let {
                    onMainClick(it)
                }
            }

            signTab.view.setOnClickListener {
                currentDid?.let {
                    onTabClick(it, "sign")
                }
            }
            commTab.view.setOnClickListener {
                currentDid?.let {
                    onTabClick(it, "comm")
                }
            }
            browserTab.view.setOnClickListener {
                currentDid?.let {
                    onTabClick(it, "browser")
                }
            }
        }

        fun onMainClick(did: DIDSelectModels.DidDisplayModel) {
            didTabs.visibility = when (didTabs.visibility) {
                View.GONE -> {
                    View.VISIBLE
                }
                else -> {
                    View.GONE
                }
            }
        }

        fun bind(did: DIDSelectModels.DidDisplayModel) {
            currentDid = did

            didTextView.text = did.did
            verkeyTextView.text = did.verkey
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DIDViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.did_item, parent, false)
        return DIDViewHolder(
            view,
            onClick
        )
    }

    override fun onBindViewHolder(holder: DIDViewHolder, position: Int) {
        val flower = getItem(position)
        holder.bind(flower)

    }
}

object DidDiffCallback : DiffUtil.ItemCallback<DIDSelectModels.DidDisplayModel>() {
    override fun areItemsTheSame(
        oldItem: DIDSelectModels.DidDisplayModel,
        newItem: DIDSelectModels.DidDisplayModel
    ): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: DIDSelectModels.DidDisplayModel,
        newItem: DIDSelectModels.DidDisplayModel
    ): Boolean {
        return oldItem.did == newItem.did
    }
}