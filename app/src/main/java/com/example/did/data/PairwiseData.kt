package com.example.did.data

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class PairwiseData(
    val label: String,
    val theirEndpoint: String,
    val theirVerkey: String,
    val myVerkey: String,
    val theirRoutingKeys: List<String>
) : Parcelable

@Keep
@Parcelize
data class PairwiseContact(
    @SerializedName("my_did") val myDid: String,
    @SerializedName("their_did") val theirDid: String,
    val metadata: PairwiseData
) : Parcelable