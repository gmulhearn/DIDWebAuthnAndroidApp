package com.example.did

import com.example.did.data.*
import com.example.did.protocols.BIP0039.generateSeed
import org.hyperledger.indy.sdk.LibIndy
import org.hyperledger.indy.sdk.crypto.Crypto
import org.hyperledger.indy.sdk.did.Did
import org.hyperledger.indy.sdk.pairwise.Pairwise
import org.hyperledger.indy.sdk.wallet.Wallet
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import com.example.did.protocols.BIP0039.generateMnemonic
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.spongycastle.jcajce.provider.asymmetric.ec.KeyFactorySpi
import java.security.spec.KeySpec
import kotlin.math.sign

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * NOTE: THIS FILE IS A MESS - DESIGNED FOR SPORADIC TESTING
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class IndyTests {
    private var openWallet: Wallet? = null
    private var openWallet2: Wallet? = null

    @Before
    fun setup() {
        LibIndy.init(File("/Users/gmulhearne/Documents/uni/dev/android/indy-sdk/libindy/target/debug/libindy.dylib"));

        val key = "5dd8DLF9GP6V9dEeQeHxsmGnBfaLxZnERrToak8sfCTJ"
        val credentials = "{\"key\":\"$key\"}"
        val config = "{\"id\":\"testID1\"}"

        openWallet = try {
            Wallet.openWallet(config, credentials).get()
        } catch (e: Exception) {
            Wallet.createWallet(config, credentials).get()
            Wallet.openWallet(config, credentials).get()
        }

        val key2 = "EqZbeZJ8uhAxcurnKaeTMPKxEfaLZXaxnReCECApaABX"
        val credentials2 = "{\"key\":\"$key2\"}"
        val config2 = "{\"id\":\"testID2\"}"

        openWallet2 = try {
            Wallet.openWallet(config2, credentials2).get()
        } catch (e: Exception) {
            Wallet.createWallet(config2, credentials2).get()
            Wallet.openWallet(config2, credentials2).get()
        }

    }

    @Test
    fun deleteWallet1() {
        val key = "5dd8DLF9GP6V9dEeQeHxsmGnBfaLxZnERrToak8sfCTJ"
        val credentials = "{\"key\":\"$key\"}"
        val config = "{\"id\":\"testID1\"}"
        openWallet!!.closeWallet().get()
        Wallet.deleteWallet(config, credentials).get()
    }

    @Test
    fun deleteWallet2() {
        val key2 = "EqZbeZJ8uhAxcurnKaeTMPKxEfaLZXaxnReCECApaABX"
        val credentials2 = "{\"key\":\"$key2\"}"
        val config2 = "{\"id\":\"testID2\"}"
        Wallet.deleteWallet(config2, credentials2).get()
    }

    @Test
    fun `gen wallet`() {
        val config = "{\"id\":\"testID2\"}"
        val key = Wallet.generateWalletKey(null).get()
        println(key)
        val credentials = "{\"key\":\"$key\"}"
        println(credentials)
        val wallet = Wallet.createWallet(config, credentials)
        println(wallet)
    }

    @Test
    fun `gen wallet 2`() {
        LibIndy.init(File("/Users/gmulhearne/Documents/uni/dev/android/indy-sdk/libindy/target/debug/libindy.dylib"));
        val key = Wallet.generateWalletKey(null).get()
        println(key)

        val credentials = "{\"key\":\"$key\"}"
        val config = "{\"id\":\"testID1\"}"

        val wallet = Wallet.createWallet(config, credentials).get()
    }

    @Test
    fun `get existing wallet`() {
        LibIndy.init(File("/Users/gmulhearne/Documents/uni/dev/android/indy-sdk/libindy/target/debug/libindy.dylib"));

        val key = "5dd8DLF9GP6V9dEeQeHxsmGnBfaLxZnERrToak8sfCTJ"
        val credentials = "{\"key\":\"$key\"}"
        val config = "{\"id\":\"testID1\"}"
        val wallet = Wallet.openWallet(config, credentials).get()

        Wallet.exportWallet(
            wallet,
            "{\"path\":\"/Users/gmulhearne/Documents/indyWalletTest\",\"key\":\"password\"}"
        ).get()

        val did = Did.createAndStoreMyDid(wallet, "{}").get()

        println("\ndid: ${did.did}\n\nverkey: ${did.verkey}\n")

        val message = "hello world"

        val signature =
            Crypto.cryptoSign(wallet, did.verkey, message.toByteArray(Charsets.UTF_8)).get()

        println("\"$message\" signature: ${signature.asList()}")

        val verify =
            Crypto.cryptoVerify(did.verkey, message.toByteArray(Charsets.UTF_8), signature).get()

        println("verify message \"$message\": $verify")

        val badVerify =
            Crypto.cryptoVerify(did.verkey, "fake message".toByteArray(Charsets.UTF_8), signature)
                .get()

        println("verify message \"fake message\": $badVerify")
    }

    @Test
    fun createAndStoreDid() {
        val myDid = Did.createAndStoreMyDid(openWallet, "{}").get()
    }

    @Test
    fun `pairwise`() {
        val myDid = Did.createAndStoreMyDid(openWallet, "{}").get()
        val theirDid = Did.createAndStoreMyDid(openWallet2, "{}").get()

        println(myDid)
        println(theirDid)

        Did.storeTheirDid(
            openWallet,
            "{\"did\":\"%s\",\"verkey\":\"%s\"}".format(theirDid.did, theirDid.verkey)
        ).get()

        println("their key: ${Did.keyForLocalDid(openWallet, theirDid.did).get()}")

//        val metadata = Gson().toJson(PairwiseData("johnno", "https://ssi-sample.com/?p=blah", theirDid.verkey, myDid.verkey)).replace("""\u003d""", "=")
//
//        Pairwise.createPairwise(openWallet, theirDid.did, myDid.did, metadata).get()
//        println(Pairwise.listPairwise(openWallet).get())
    }

    @Test
    fun `store and get WebAuthn DID`() {
        val myDid = Did.createAndStoreMyDid(openWallet, "{}").get()
        val webauthnMeta = WebAuthnDIDData(
            "keyabc123",
            1,
            PublicKeyCredentialUserEntity(byteArrayOf(1), "user1", "gm"),
            RelyingPartyInfo("RPorg", "RPID"),
            myDid.verkey,
            myDid.did
        )

        // val webauthnMeta = Gson().toJson(PairwiseData("johnno", "https://ssi-sample.com/?p=blah", myDid.verkey, myDid.verkey, listOf(), false)).replace("""\u003d""", "=")

        val metaJSON = Gson().toJson(webauthnMeta)

        println(metaJSON)

        Did.setDidMetadata(openWallet, myDid.did, metaJSON).get()

        val myDids = Did.getListMyDidsWithMeta(openWallet).get()
        val metadataDIDType = object : TypeToken<List<MetadataDID>>() {}.type
        val didList = Gson().fromJson<List<MetadataDID>>(myDids, metadataDIDType)

        println(didList)
    }

    @Test
    fun `list DIDs metadata`() {
        val myDids = Did.getListMyDidsWithMeta(openWallet).get()
        val metadataDIDType = object : TypeToken<List<MetadataDID>>() {}.type
        val didList = Gson().fromJson<List<MetadataDID>>(myDids, metadataDIDType)

        println(didList.joinToString(",\n") { it.toString() })

        println("\n\n")

        didList.forEach { metaDid ->
            metaDid.metadata?.let {
                try {
                    val webauthnMeta = Gson().fromJson(it, WebAuthnDIDData::class.java)
                    println(webauthnMeta)
                } catch (e: Exception) {

                }
            }
        }

        val validDids = didList.filter { metaDid ->
            var isWebAuthnMeta = false
            metaDid.metadata?.let {
                try {
                    Gson().fromJson(it, WebAuthnDIDData::class.java)
                    isWebAuthnMeta = true
                } catch (e: Exception) {
                }
            }
            isWebAuthnMeta
        }

        println(validDids)
    }

    @Test
    fun `messaging`() {
        val myDids = Did.getListMyDidsWithMeta(openWallet).get()
        val myDid = JSONObject("{ \"dids\": $myDids}").getJSONArray("dids").getJSONObject(0)
        val theirDids = Did.getListMyDidsWithMeta(openWallet2).get()
        val theirDid = JSONObject("{ \"dids\": $theirDids}").getJSONArray("dids").getJSONObject(0)

        val message = "hello world"

        val recipVKs = "[\"${theirDid.getString("verkey")}\"]"
        val encryptedMsg = Crypto.packMessage(
            openWallet,
            recipVKs,
            myDid.getString("verkey"),
            message.toByteArray(Charsets.UTF_8)
        ).get()

        val signature = Crypto.cryptoSign(
            openWallet,
            myDid.getString("verkey"),
            "hello".toByteArray(Charsets.UTF_8)
        ).get()

        println(signature.toString(Charsets.UTF_8))
        for (b in signature) {
            val st = String.format("%02X", b)
            print(st)
        }

        println(encryptedMsg.toString(Charsets.UTF_8))

        val decryptedMsg = Crypto.unpackMessage(openWallet2, encryptedMsg).get()
        println(decryptedMsg.toString(Charsets.UTF_8))
    }

    @ExperimentalUnsignedTypes
    fun generateMnemonic(numEntBits: Int = 128): List<String> {
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
            this.javaClass.classLoader.getResource("bip0039wordlist.txt").readText(Charsets.UTF_8)
                .split("\n")

        return wordIntList.map { index -> wordList[index] }
    }

//    @ExperimentalUnsignedTypes
//    fun generateSeed(wordList: List<String>): ByteArray {
//
//        val spec = PBEKeySpec(
//            "".toCharArray(),
//            wordList.joinToString("") { it }.toByteArray(Charsets.UTF_8),
//            2048,
//            512
//        )
//        val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
//
//        return keyFactory.generateSecret(spec).encoded
//    }

    @ExperimentalUnsignedTypes
    @Test
    fun `gen_seed`() {
        val seedWords = generateMnemonic()
        println(seedWords)
        val seedBytes = generateSeed(seedWords)
        println(seedBytes.toUByteArray().toList())
    }

    @ExperimentalUnsignedTypes
    @Test
    fun `test wallet did seed diffs`() {
        val seed = Base64.getEncoder().encode(
            generateSeed(
                generateMnemonic(128)
            )
        ).toString(Charsets.UTF_8)
        println(seed)

        val didJsonMap = mutableMapOf<String, Any?>(
            Pair("did", null),
            Pair("seed", seed.subSequence(0, 32)),
            Pair("crypto_type", null),
            Pair("cid", null),
            Pair("method_name", null)
        )

        val didJson = JSONObject(didJsonMap)

        println(didJson.toString())

        val did1 = Did.createAndStoreMyDid(openWallet, didJson.toString()).get()
        val did2 = Did.createAndStoreMyDid(openWallet2, didJson.toString()).get()

        println("${did1.did} ${did2.did}\n${did1.verkey} ${did2.verkey}")
    }

    @Test
    fun base_64_test_random() {
        val encoded =
            "AAAAAGBF2w17IkRJRCI6IlI2SnJ4WDduREo1YUhkdUw2ampzODMiLCJESUREb2MiOnsiaWQiOiJSNkpyeFg3bkRKNWFIZHVMNmpqczgzIiwicHVibGljS2V5IjpbeyJpZCI6IlI2SnJ4WDduREo1YUhkdUw2ampzODMja2V5cy0xIiwicHVibGljS2V5QmFzZTU4IjoiRThhZkU1VjEzd1dyMUJSZEcxSllIRFZWMkdoZFhpdURZS3A1a1I1Y0dIVWQiLCJ0eXBlIjoiRWQyNTUxOVZlcmlmaWNhdGlvbktleTIwMTgiLCJjb250cm9sbGVyIjoiUjZKcnhYN25ESjVhSGR1TDZqanM4MyJ9XSwiQGNvbnRleHQiOiJodHRwczpcL1wvd3d3LnczLm9yZ1wvbnNcL2RpZFwvdjEiLCJzZXJ2aWNlIjpbeyJpZCI6IlI2SnJ4WDduREo1YUhkdUw2ampzODM7aW5keSIsInJvdXRpbmdLZXlzIjpbXSwidHlwZSI6IkluZHlBZ2VudCIsInNlcnZpY2VFbmRwb2ludCI6Imh0dHBzOlwvXC91cy1jZW50cmFsMS1kaWRzYW1wbGUtNjI5NzYuY2xvdWRmdW5jdGlvbnMubmV0XC9lbmRwb2ludD9wPTE3RDBDNEUwLTNGNzgtNEQ4QS05NTEzLUQ4MjcyNzc0Rjk0QSIsInJlY2lwaWVudEtleXMiOlsiRThhZkU1VjEzd1dyMUJSZEcxSllIRFZWMkdoZFhpdURZS3A1a1I1Y0dIVWQiXX1dfX0"
        val result = (Base64.getDecoder().decode(encoded).toString(Charsets.UTF_8)).drop(8)
        val obj = Gson().fromJson(result, DIDRequestConnection::class.java)
        println(obj.did)
        println(obj)
    }

    @Test
    fun keyspecTest() {
    }
}