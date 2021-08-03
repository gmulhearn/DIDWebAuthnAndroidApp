package com.gmulhearn.didwebauthn.data.indy

import android.os.Parcelable
import androidx.annotation.Keep
import com.gmulhearn.didwebauthn.data.PublicKeyCredentialUserEntity
import com.gmulhearn.didwebauthn.data.RelyingPartyInfo
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