package com.gmulhearn.didwebauthn.data.indy

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class RelayDIDData(
    val postboxID: String
): Parcelable
