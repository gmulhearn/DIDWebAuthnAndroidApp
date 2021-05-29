package com.example.did.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MetadataDID(
    val did: String,
    val verkey: String,
    val tempVerkey: String?,
    val metadata: String?
) : Parcelable
