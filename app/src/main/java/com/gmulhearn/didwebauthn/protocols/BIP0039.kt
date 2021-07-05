package com.gmulhearn.didwebauthn.protocols

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import org.spongycastle.jce.provider.BouncyCastleProvider
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.Security
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object BIP0039 {
    @RequiresApi(Build.VERSION_CODES.O)
    @ExperimentalUnsignedTypes
    fun generateMnemonic(numEntBits: Int = 128, context: Context): List<String> {
        val ent = SecureRandom.getInstanceStrong().generateSeed(numEntBits / 8)

        val entString = ent.toUByteArray().joinToString("") {
            val bits = it.toString(2)
            "0".repeat(8 - bits.length) + bits
        }

        val md = MessageDigest.getInstance("SHA-256")
        val hashedEnt = md.digest(ent)

        val hashedEntString = hashedEnt.toUByteArray().joinToString("") {
            val bits = it.toString(2)
            "0".repeat(8 - bits.length) + bits
        }

        val checksum = hashedEntString.subSequence(0, numEntBits / 32)
        val concatBits = entString + checksum
        val numWords = concatBits.length / 11

        val wordIntList = (0 until numWords).map { i ->
            val wordStr = concatBits.substring(11 * (numWords - 1 - i), 11 * (numWords - i))
            wordStr.toInt(2)
        }.toList().reversed()

        val wordList =
            context.assets.open("bip0039wordlist.txt").readBytes().toString(Charsets.UTF_8).split("\n")

        return wordIntList.map { index -> wordList[index] }
    }

    @ExperimentalUnsignedTypes
    fun generateSeed(wordList: List<String>): ByteArray {

        val spec = PBEKeySpec(
            " ".toCharArray(),
            wordList.joinToString("") { it }.toByteArray(Charsets.UTF_8),
            2048,
            512
        )

        (Security.getProviders().forEach { println(it.name) })
        // val provider = Security.getProvider("SunJCE")

        val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512", BouncyCastleProvider())

        return keyFactory.generateSecret(spec).encoded
    }
}