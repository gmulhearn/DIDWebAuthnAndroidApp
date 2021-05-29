package com.example.did.data

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class WebAuthnDIDData(
    val keyId: String,
    val authCounter: Int,
    val userInfo: PublicKeyCredentialUserEntity,
    val rpInfo: RelyingPartyInfo,
    val edDSAKey: String, // base58 string (did verkey)
    val did: String
) : Parcelable