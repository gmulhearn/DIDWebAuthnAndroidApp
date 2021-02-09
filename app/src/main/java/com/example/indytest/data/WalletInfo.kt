package com.example.indytest.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WalletInfo(val config: String, val credentials : String) : Parcelable