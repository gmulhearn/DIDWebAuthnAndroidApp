package com.example.did.protocols

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.did.data.*
import com.google.gson.Gson
import duo.labs.webauthn.models.RpEntity
import duo.labs.webauthn.models.UserEntity
import org.spongycastle.jcajce.provider.digest.SHA256
import java.util.*

fun Map<String, Int>.toByteArray(): ByteArray {
    val array = ByteArray(entries.size)
    entries.forEach { entry ->
        array[entry.key.toInt()] = entry.value.toByte()
    }
    return array
}

fun UserInfo.getId(): ByteArray {
    return mappedId.toByteArray()
}

fun CredentialCreationOptions.getChallenge(): ByteArray {
    return mappedChallenge.toByteArray()
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

/**
 * Transform into AuthenticatorMakeCredentialOptions object for the authenticator
 */
@RequiresApi(Build.VERSION_CODES.O)
fun CredentialCreationOptions.toAuthenticatorMakeCredentialOptions(origin: String): AuthenticatorMakeCredentialOptions {
    val clientData = CollectedClientData(
        type = "webauthn.create",
        challengeBase64URL = Base64.getUrlEncoder().encodeToString(getChallenge()),
        origin = origin
    )

    val clientDataHash = clientData.hash()
    val rp = PublicKeyCredentialRpEntity(relyingPartyInfo.id, relyingPartyInfo.name)
    val user = PublicKeyCredentialUserEntity(user.getId(), user.displayName, user.name)
    val pubKeyCredParams = this.pubKeyCredParams.map { Pair(it.type, it.algorithm.toLong()) }

    return AuthenticatorMakeCredentialOptions(clientDataHash, rp, user, pubKeyCredParams)
}

/**
 * TODO: BELOW IS TEMP FOR TESTING/DEBUGGING - REMOVE IN FUTURE
 * Transformer for converting this AuthenticatorMakeCredentialOptions to DuoLabs format
 */
fun AuthenticatorMakeCredentialOptions.toDuoLabsAuthn(
): duo.labs.webauthn.models.AuthenticatorMakeCredentialOptions {
    val duoLabs = duo.labs.webauthn.models.AuthenticatorMakeCredentialOptions()
    val duoLabsRP = RpEntity()
    duoLabsRP.id = rp.id
    duoLabsRP.name = rp.name
    val duoLabsUser = UserEntity()
    duoLabsUser.displayName = user.displayName
    duoLabsUser.id = user.id
    duoLabsUser.name = user.name

    duoLabs.clientDataHash = clientDataHash
    duoLabs.rpEntity = duoLabsRP
    duoLabs.userEntity = duoLabsUser

    duoLabs.credTypesAndPubKeyAlgs = pubKeyCredParams as MutableList<android.util.Pair<String, Long>>
    duoLabs.excludeCredentialDescriptorList = mutableListOf()
    duoLabs.requireResidentKey = false
    duoLabs.requireUserPresence = true  // ?? for wellformed()
    duoLabs.requireUserVerification = false

    return duoLabs
}

fun createPublicKeyCredential(
    rawId: ByteArray,
    attestationObject: ByteArray,
    clientDataJson: ByteArray
): PublicKeyCredential {
    val attestationResponse = AuthenticatorAttestationResponse(
        clientDataJSON = clientDataJson,
        attestationObject = attestationObject
    )
    return PublicKeyCredential(
        rawId = rawId,
        id = Base64.getUrlEncoder().encodeToString(rawId),
        response = attestationResponse
    )
}

fun PublicKeyCredential.JSON(): String {
    return Gson().toJson(this) ?: "{}"
}