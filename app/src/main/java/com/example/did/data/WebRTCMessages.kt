package com.example.did.data

import androidx.annotation.Keep

/**
 * set of constant strings that represent the type of message
 * docstrings attached describe the expected type of the data field, so they can be cast
 */
object WebRTCMessageTypes {
    /** data type = [PublicKeyCredentialCreationOptions] */
    const val WEBAUTHN_REG_REQUEST = "WEBAUTHN_REG_REQUEST"

    /** data type = [PublicKeyCredentialAttestationResponse] */
    const val WEBAUTHN_REG_RESPONSE = "WEBAUTHN_REG_RESPONSE"

    /** data type = [PublicKeyCredentialRequestOptions] */
    const val WEBAUTHN_AUTH_REQUEST = "WEBAUTHN_AUTH_REQUEST"

    /** data type = [PublicKeyCredentialAssertionResponse] */
    const val WEBAUTHN_AUTH_RESPONSE = "WEBAUTHN_AUTH_RESPONSE"

    const val INVALID = "INVALID"
}

/**
 * generalised data structure for incoming webrtc messages (from server)
 */
@Keep
data class WRTCBaseMessageIn(
    val type: String,
    val jsonData: String
)

/**
 * generalised data structure for outgoing webrtc messages (to server)
 */
@Keep
data class WRTCBaseMessageOut(
    val type: String,
    val data: Any
)

@Keep
data class WRTCPublicKeyCredentialRequestOptions(
    val publicKeyCredentialRequestOptions: PublicKeyCredentialRequestOptions,
    val origin: String
)

@Keep
data class WRTCPublicKeyCredentialCreationOptions(
    val publicKeyCredentialCreationOptions: PublicKeyCredentialCreationOptions,
    val origin: String
)