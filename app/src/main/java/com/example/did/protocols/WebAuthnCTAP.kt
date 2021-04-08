package com.example.did.protocols

import com.example.did.data.*

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
fun CollectedClientData.hash(): ByteArray {
    var result = "" // 1.
    result += "{\"type\":" //2.

    return result.toByteArray(Charsets.UTF_8) // TODO WRONG
}

fun CredentialCreationOptions.toAuthenticatorMakeCredentialOptions(): AuthenticatorMakeCredentialOptions {
    val clientDataHash = ByteArray(1)
    // TODO!!!!
    val rp = PublicKeyCredentialRpEntity("", "")
    val user = PublicKeyCredentialUserEntity(byteArrayOf(), "", "")
    val pubKeyCredParams = listOf(Pair<String, Long>("public-key-todo", -8))

    return AuthenticatorMakeCredentialOptions(clientDataHash, rp, user, pubKeyCredParams)
}