package com.example.did.protocols

import com.example.did.data.CredentialCreationOptions
import com.example.did.data.UserInfo

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