package com.example.did.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DidInfo(val did: String, val verkey: String) : Parcelable
