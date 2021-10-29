package com.gmulhearn.didwebauthn.core.protocols

import android.os.Build
import androidx.annotation.RequiresApi
import com.gmulhearn.didwebauthn.data.*
import com.google.gson.Gson
import org.json.JSONObject
import org.spongycastle.jcajce.provider.digest.SHA256
import java.net.URL
import java.util.*

fun Map<String, Int>.toByteArray(): ByteArray {
    val array = ByteArray(entries.size)
    entries.forEach { entry ->
        array[entry.key.toInt()] = entry.value.toByte()
    }
    return array
}

/**
 * https://www.w3.org/TR/webauthn/#ccdtostring
 */
fun String.CCDToString(): String {
    val normalSet = mutableListOf(0x0020, 0x0021)
    normalSet.addAll(0x0023..0x005B)
    normalSet.addAll(0x005D..0x10FFFF)
    var result = "" // 1.
    result += "\""  // 2.
    toByteArray(Charsets.UTF_8).forEach {
        val value = it.toInt()
        result += when (value) {
            in normalSet -> byteArrayOf(it).toString(Charsets.UTF_8)
            0x0022 -> """\""""
            0x005C -> """\\"""
            else -> """\u""" + value.toString(16).padStart(4, '0').toLowerCase()
        }
    }
    result += "\""
    return result
}

/**
 * https://www.w3.org/TR/webauthn/#clientdatajson-serialization
 */
fun CollectedClientData.JSON(): String {
    var result = ""                                 // 1.
    result += "{\"type\":"                          // 2.
    result += type.CCDToString()                    // 3.
    result += ",\"challenge\":"                     // 4.
    result += challengeBase64URL.CCDToString()      // 5.
    result += ",\"origin\":"                        // 6.
    result += origin.CCDToString()                  // 7.
    result += ",\"crossOrigin\":"                   // 8.
    result += "false"                               // 9.
    result += "}"                                   // 12.
    return result
}

/**
 * https://www.w3.org/TR/webauthn/#clientdatajson-serialization
 */
fun CollectedClientData.hash(): ByteArray {
    val rawResult = JSON().toByteArray(Charsets.UTF_8)
    return SHA256.Digest().digest(rawResult)
}

/***************************** Credential Creation ***********************/

fun UserInfo.getId(): ByteArray {
    return mappedId.toByteArray()
}

fun CredentialCreationOptions.getChallenge(): ByteArray {
    return mappedChallenge.toByteArray()
}

/**
 * Standardises way to assemble [CollectedClientData] properly
 */
fun createCollectedClientData(
    isRegistration: Boolean,
    challenge: ByteArray,
    origin: String
): CollectedClientData {
    return CollectedClientData(
        type = if (isRegistration) "webauthn.create" else "webauthn.get",
        challengeBase64URL = Base64.getUrlEncoder().withoutPadding().encodeToString(challenge),
        origin = origin
    )
}

/**
 * Transform into AuthenticatorMakeCredentialOptions object for the authenticator
 */
@RequiresApi(Build.VERSION_CODES.O)
fun CredentialCreationOptions.toAuthenticatorMakeCredentialOptions(origin: String): AuthenticatorMakeCredentialOptions {
    val clientData = createCollectedClientData(true, getChallenge(), origin)

    // rpId = the id if specified (should check), else use the host/domain
    val rpId = relyingPartyInfo.id ?:
        URL(origin).host

    val clientDataHash = clientData.hash()
    val rp = PublicKeyCredentialRpEntity(rpId, relyingPartyInfo.name)
    val user = PublicKeyCredentialUserEntity(user.getId(), user.displayName, user.name)
    val pubKeyCredParams = this.pubKeyCredParams.map { Pair(it.type, it.algorithm.toLong()) }

    return AuthenticatorMakeCredentialOptions(clientDataHash, rp, user, pubKeyCredParams)
}

fun createPublicKeyCredentialAttestationResponse(
    rawId: ByteArray,
    attestationObject: ByteArray,
    clientDataJson: ByteArray
): PublicKeyCredentialAttestationResponse {
    val attestationResponse = AuthenticatorAttestationResponse(
        clientDataJSON = clientDataJson,
        attestationObject = attestationObject
    )
    return PublicKeyCredentialAttestationResponse(
        rawId = rawId,
        id = Base64.getUrlEncoder().encodeToString(rawId).removeSuffix("=").removeSuffix("="),
        response = attestationResponse
    )
}

fun PublicKeyCredentialAttestationResponse.JSON(): String {
    return Gson().toJson(this) ?: "{}"
}

fun createPublicKeyCredentialAssertionResponse(
    rawId: ByteArray,
    authenticatorData: ByteArray,
    clientDataJson: ByteArray,
    signature: ByteArray,
    userHandle: ByteArray
): PublicKeyCredentialAssertionResponse {
    val assertionResponse = AuthenticatorAssertionResponse(
        authenticatorData = authenticatorData,
        clientDataJSON = clientDataJson,
        signature = signature,
        userHandle = userHandle
    )

    return PublicKeyCredentialAssertionResponse(
        rawId = rawId,
        id = Base64.getUrlEncoder().encodeToString(rawId).removeSuffix("=").removeSuffix("="),
        response = assertionResponse
    )
}

fun PublicKeyCredentialAssertionResponse.JSON(): String {
    return Gson().toJson(this) ?: "{}"
}

/**
 * special base for where bytearrays should be reduced to base64
 */
fun PublicKeyCredentialAssertionResponse.base64JSON(): String {
    val fullJSON = Gson().toJson(this)

    val base64JSON = JSONObject(fullJSON)

    base64JSON.getJSONObject("response")
        .put("clientDataJSON", Base64.getEncoder().encodeToString(this.response.clientDataJSON))
    base64JSON.getJSONObject("response")
        .put("signature", Base64.getEncoder().encodeToString(this.response.signature))
    base64JSON.getJSONObject("response").put(
        "authenticatorData",
        Base64.getEncoder().encodeToString(this.response.authenticatorData)
    )

    println(base64JSON)

    return "*${base64JSON.toString()}"  // * to indicate for browser..
}

/*******************************************************************/

/***************************** Credential Fetch ***********************/

fun AllowCredentialDescriptor.getId(): ByteArray {
    return mappedId.toByteArray()
}

fun CredentialRequestOptions.getChallenge(): ByteArray {
    return mappedChallenge.toByteArray()
}

fun CredentialRequestOptions.toAuthenticatorGetAssertionOptions(origin: String): AuthenticatorGetAssertionOptions {
    val clientData = createCollectedClientData(
        false,
        getChallenge(),
        origin
    )
    val clientDataHash = clientData.hash()

    // val urlObj = URL(origin)

    // TODO: IMPORTANT SECURITY: origin rpID check

    return AuthenticatorGetAssertionOptions(
        rpId = rpId, // urlObj.host, // TODO need to check this works
        clientDataHash = clientDataHash,
        allowCredentialDescriptorList = allowCredentials,
        requireUserPresence = true,
        requireUserVerification = false
    )
}


/*******************************************************************/
