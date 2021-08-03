package com.gmulhearn.didwebauthn.data.indy

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LibIndyDIDListItem(
    val did: String,
    val verkey: String,
    val tempVerkey: String?,
    val metadata: String?
) : Parcelable

@Parcelize
data class DIDMetaData(
    val webAuthnData: WebAuthnDIDData? = null,
    val relayDIDData: RelayDIDData? = null
) : Parcelable