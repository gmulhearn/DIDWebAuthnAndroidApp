package com.gmulhearn.didwebauthn.data.indy

import android.os.Parcelable
import androidx.annotation.Keep
import com.gmulhearn.didwebauthn.data.PublicKeyCredentialUserEntity
import com.gmulhearn.didwebauthn.data.RelyingPartyInfo
import kotlinx.android.parcel.Parcelize

const val EDDSA_ALG = -8
const val ES256_ALG = -7

enum class KEY_ALG(val value: Int) {
    EDDSA(-8),
    ES256(-7)
}

fun Int.toSupportedKeyAlg(): KEY_ALG {
    return when (this) {
        EDDSA_ALG -> KEY_ALG.EDDSA
        ES256_ALG -> KEY_ALG.ES256
        else -> throw Exception("unsupported key alg: $this")
    }
}

@Keep
@Parcelize
data class WebAuthnDIDData(
    val keyId: String,
    val authCounter: Int,
    val userInfo: PublicKeyCredentialUserEntity,
    val rpInfo: RelyingPartyInfo,
    val edDSAKey: String, // base58 string (did verkey)
    val did: String,
    val keyAlg: KEY_ALG = KEY_ALG.EDDSA // default to EdDSA type key. but also support -7.
) : Parcelable