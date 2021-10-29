package com.gmulhearn.didwebauthn

import android.os.Environment
import com.gmulhearn.didwebauthn.data.*
import com.gmulhearn.didwebauthn.core.protocols.BIP0039.generateSeed
import com.gmulhearn.didwebauthn.data.indy.DIDMetaData
import com.gmulhearn.didwebauthn.data.indy.LibIndyDIDListItem
import com.gmulhearn.didwebauthn.data.indy.WebAuthnDIDData
import org.hyperledger.indy.sdk.LibIndy
import org.hyperledger.indy.sdk.crypto.Crypto
import org.hyperledger.indy.sdk.did.Did
import org.hyperledger.indy.sdk.wallet.Wallet
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import java.io.File
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.hyperledger.indy.sdk.pool.Pool
import org.hyperledger.indy.sdk.pool.PoolJSONParameters
import kotlin.concurrent.timer

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * NOTE: THIS FILE IS A MESS - DESIGNED FOR SPORADIC TESTING
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class DIDWebAuthnAndroids {
    private var openWallet: Wallet? = null
    private var openWallet2: Wallet? = null

    companion object {
        const private val BUILDER_NET_CONFIG = "{\"reqSignature\":{},\"txn\":{\"data\":{\"data\":{\"alias\":\"FoundationBuilder\",\"blskey\":\"3gmhmqpPLqznZF3g3niodaHjbpsB6TEeE9SpgXgBnZJLmXgeRzJqTLajVwbhxrkomJFTFU4ohDC4ZRXKbUPCQywJuPAQnst8XBtCFredMECn4Z3goi1mNt5QVRdU8Ue2xMSkdLpsQMjCsNwYUsBguwXYUQnDXQXnHqRkK9qrivucQ5Z\",\"blskey_pop\":\"RHWacPhUNc9JWsGNdmWYHrAvvhsow399x3ttNKKLDpz9GkxxnTKxtiZqarkx4uP5ByTwF4kM8nZddFKWuzoKizVLttALQ2Sc2BNJfRzzUZMNeQSnESkKZ7U5vE2NhUDff6pjANczrrDAXd12AjSG61QADWdg8CVciZFYtEGmKepwzP\",\"client_ip\":\"35.161.146.16\",\"client_port\":\"9702\",\"node_ip\":\"50.112.53.5\",\"node_port\":\"9701\",\"services\":[\"VALIDATOR\"]},\"dest\":\"GVvdyd7Y6hsBEy5yDDHjqkXgH8zW34K74RsxUiUCZDCE\"},\"metadata\":{\"from\":\"V5qJo72nMeF7x3ci8Zv2WP\"},\"type\":\"0\"},\"txnMetadata\":{\"seqNo\":1,\"txnId\":\"fe991cd590fff10f596bb6fe2362229de47d49dd50748e38b96f368152be29c7\"},\"ver\":\"1\"}\n" +
                "{\"reqSignature\":{},\"txn\":{\"data\":{\"data\":{\"alias\":\"vnode1\",\"blskey\":\"t5jtREu8au2dwFwtH6QWopmTGxu6qmJ3iSnk321yLgeu7mHQRXf2ZCBuez8KCAQvFZGqqAoy2FcYvDGCqQxRCz9qXKgiBtykzxjDjYu87JECwwddnktz5UabPfZmfu6EoDn4rFxvd4myPu2hksb5Z9GT6UeoEYi7Ub3yLFQ3xxaQXc\",\"blskey_pop\":\"QuHB7tiuFBPQ6zPkwHfMtjzWqXJBLACtfggm7zCRHHgdva18VN4tNg7LUU2FfKGQSLZz1M7oRxhhgJkZLL19aGvaHB2MPtnBWK9Hr8LMiwi95UjX3TVXJri4EvPjQ6UUvHrjZGUFvKQphPyVTMZBJwfkpGAGhpbTQuQpEH7f56m1X5\",\"client_ip\":\"206.189.143.34\",\"client_port\":\"9796\",\"node_ip\":\"206.189.143.34\",\"node_port\":\"9797\",\"services\":[\"VALIDATOR\"]},\"dest\":\"9Aj2LjQ2fwszJRSdZqg53q5e6ayScmtpeZyPGgKDswT8\"},\"metadata\":{\"from\":\"FzAaV9Waa1DccDa72qwg13\"},\"type\":\"0\"},\"txnMetadata\":{\"seqNo\":2,\"txnId\":\"5afc282bf9a7a5e3674c09ee48e54d73d129aa86aa226691b042e56ff9eaf59b\"},\"ver\":\"1\"}\n" +
                "{\"reqSignature\":{},\"txn\":{\"data\":{\"data\":{\"alias\":\"xsvalidatorec2irl\",\"blskey\":\"4ge1yEvjdcV6sDSqbevqPRWq72SgkZqLqfavBXC4LxnYh4QHFpHkrwzMNjpVefvhn1cgejHayXTfTE2Fhpu1grZreUajV36T6sT4BiewAisdEw59mjMxkp9teYDYLQqwPUFPgaGKDbFCUBEaNdAP4E8Q4UFiF13Qo5842pAY13mKC23\",\"blskey_pop\":\"R5PoEfWvni5BKvy7EbUbwFMQrsgcuzuU1ksxfvySH6FC5jpmisvcHMdVNik6LMvAeSdt6K4sTLrqnaaQCf5aCHkeTcQRgDVR7oFYgyZCkF953m4kSwUM9QHzqWZP89C6GkBx6VPuL1RgPahuBHDJHHiK73xLaEJzzFZtZZxwoWYABH\",\"client_ip\":\"52.50.114.133\",\"client_port\":\"9702\",\"node_ip\":\"52.209.6.196\",\"node_port\":\"9701\",\"services\":[\"VALIDATOR\"]},\"dest\":\"DXn8PUYKZZkq8gC7CZ2PqwECzUs2bpxYiA5TWgoYARa7\"},\"metadata\":{\"from\":\"QuCBjYx4CbGCiMcoqQg1y\"},\"type\":\"0\"},\"txnMetadata\":{\"seqNo\":3,\"txnId\":\"1972fce7af84b7f63b7f0c00495a84425cce3b0c552008576e7996524cca04cb\"},\"ver\":\"1\"}\n" +
                "{\"reqSignature\":{},\"txn\":{\"data\":{\"data\":{\"alias\":\"danube\",\"blskey\":\"3Vt8fxn7xg8n8pR872cvGWNuR7STFzFSPMftX96zF6871wYVTR27aspxGSeEtx9wj8g4D3GdCxHJbQ4FsxQz6TATQswiiZfxAVNjLLUci8WSH4t1GPx9CvGXB2uzDfVnnJyhhnASxJEbvykLUBBFG3fW4tMQixujpowUADz5jHm427u\",\"blskey_pop\":\"RJpXXLkjRRv9Lk8tJz8LTkhhC7RWjHQcB9CG8J8U8fXT6arTDMYc62zXtToBAmGkGu8Udsmo3Hh7mv4KB9JAf8ufGY9WsnppCVwar7zEXyBfLpCnDhvVcBAzkhRpHmqHygN24DeBu9aH6tw4uXxVJvRRGSbPtxjWa379BmfQWzXHCb\",\"client_ip\":\"207.180.207.73\",\"client_port\":\"9702\",\"node_ip\":\"173.249.14.196\",\"node_port\":\"9701\",\"services\":[\"VALIDATOR\"]},\"dest\":\"52muwfE7EjTGDKxiQCYWr58D8BcrgyKVjhHgRQdaLiMw\"},\"metadata\":{\"from\":\"VbPQNHsvoLZdaNU7fTBeFx\"},\"type\":\"0\"},\"txnMetadata\":{\"seqNo\":4,\"txnId\":\"ebf340b317c044d970fcd0ca018d8903726fa70c8d8854752cd65e29d443686c\"},\"ver\":\"1\"}"
    }

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
            PublicKeyCredentialRpEntity("RPorg", "RPID"),
            myDid.verkey,
            myDid.did
        )

        // val webauthnMeta = Gson().toJson(PairwiseData("johnno", "https://ssi-sample.com/?p=blah", myDid.verkey, myDid.verkey, listOf(), false)).replace("""\u003d""", "=")

        val metaJSON = Gson().toJson(webauthnMeta)

        println(metaJSON)

        Did.setDidMetadata(openWallet, myDid.did, metaJSON).get()

        val myDids = Did.getListMyDidsWithMeta(openWallet).get()
        val metadataDIDType = object : TypeToken<List<LibIndyDIDListItem>>() {}.type
        val didList = Gson().fromJson<List<LibIndyDIDListItem>>(myDids, metadataDIDType)

        println(didList)
    }

    @Test
    fun `list DIDs metadata`() {
        val myDids = Did.getListMyDidsWithMeta(openWallet).get()
        val metadataDIDType = object : TypeToken<List<LibIndyDIDListItem>>() {}.type
        val didList = Gson().fromJson<List<LibIndyDIDListItem>>(myDids, metadataDIDType)

        println(didList.joinToString(",\n") { it.toString() })

        println("\n\n")

        didList.forEach { metaDid ->
            metaDid.metadata?.let {
                try {
                    val webauthnMeta = Gson().fromJson(it, DIDMetaData::class.java)
                    println(webauthnMeta)
                } catch (e: Exception) {

                }
            }
        }

        val validDids = didList.filter { metaDid ->
            var isWebAuthnMeta = false
            metaDid.metadata?.let {
                try {
                    val metadata = Gson().fromJson(it, DIDMetaData::class.java)
                    isWebAuthnMeta = metadata.webAuthnData != null
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
    fun relaySecurityInitWithDid1And1234Connection() = runBlocking<Unit> {
        // get my first did
        val myDids = Did.getListMyDidsWithMeta(openWallet).get()
        val myDidJSON = JSONObject("{ \"dids\": $myDids}").getJSONArray("dids").getJSONObject(0)
        val verkey = myDidJSON.get("verkey").toString()

        val initJSON = JSONObject(
            mutableMapOf(
                Pair("connectionId", "1234-1234-1234-1234"),
                Pair("key", verkey)
            ) as Map<*, *>
        )
        postToLocalTest(initJSON.toString(), "init")
    }

    @Test
    fun relaySecurityValidTest() = runBlocking<Unit> {
        // get my first did
        val myDids = Did.getListMyDidsWithMeta(openWallet).get()
        val myDidJSON = JSONObject("{ \"dids\": $myDids}").getJSONArray("dids").getJSONObject(0)
        val verkey = myDidJSON.get("verkey").toString()

        val timestamp = Date().time.toString()
        val timestampSig =
            Crypto.cryptoSign(openWallet, verkey, timestamp.toByteArray(Charsets.UTF_8)).get()

        val getMessagesJSON = JSONObject(
            mutableMapOf(
                Pair("connectionId", "1234-1234-1234-1234"),
                Pair("timestamp", timestamp),
                Pair("timestampSig", Base64.getEncoder().encodeToString(timestampSig))
            ) as Map<*, *>
        )
        postToLocalTest(getMessagesJSON.toString(), "getMessages")
    }

    @Test
    fun relaySecurityInvalidSigTest() = runBlocking<Unit> {
        // get incorrect did
        val myDids = Did.getListMyDidsWithMeta(openWallet).get()
        val incorrectDidJSON = JSONObject("{ \"dids\": $myDids}").getJSONArray("dids").getJSONObject(1)
        val incorrectSigningVerkey = incorrectDidJSON.get("verkey").toString()

        // sign with incorrect Did
        val timestamp = Date().time.toString()
        val timestampSig =
            Crypto.cryptoSign(openWallet, incorrectSigningVerkey, timestamp.toByteArray(Charsets.UTF_8)).get()

        val getMessagesJSON = JSONObject(
            mutableMapOf(
                Pair("connectionId", "1234-1234-1234-1234"),
                Pair("timestamp", timestamp),
                Pair("timestampSig", Base64.getEncoder().encodeToString(timestampSig))
            ) as Map<*, *>
        )
        postToLocalTest(getMessagesJSON.toString(), "getMessages")
    }

    @Test
    fun relaySecurityReplayAttackTest() = runBlocking<Unit> {
        // get my first Did
        val myDids = Did.getListMyDidsWithMeta(openWallet).get()
        val myDidJSON = JSONObject("{ \"dids\": $myDids}").getJSONArray("dids").getJSONObject(0)
        val verkey = myDidJSON.get("verkey").toString()

        // sign valid
        val timestamp = Date().time.toString()
        val timestampSig =
            Crypto.cryptoSign(openWallet, verkey, timestamp.toByteArray(Charsets.UTF_8)).get()
        val getMessagesJSON = JSONObject(
            mutableMapOf(
                Pair("connectionId", "1234-1234-1234-1234"),
                Pair("timestamp", timestamp),
                Pair("timestampSig", Base64.getEncoder().encodeToString(timestampSig))
            ) as Map<*, *>
        )
        // post once, valid
        postToLocalTest(getMessagesJSON.toString(), "getMessages")
        // post twice, invalid
        postToLocalTest(getMessagesJSON.toString(), "getMessages")
    }

    @Test
    fun relaySecurityInvalidClockTest() = runBlocking<Unit> {
        // get my first Did
        val myDids = Did.getListMyDidsWithMeta(openWallet).get()
        val myDidJSON = JSONObject("{ \"dids\": $myDids}").getJSONArray("dids").getJSONObject(0)
        val verkey = myDidJSON.get("verkey").toString()

        // sign old timestamp
        val timestamp = (Date().time + (2 * 60 * 60 * 60)).toString()
        val timestampSig =
            Crypto.cryptoSign(openWallet, verkey, timestamp.toByteArray(Charsets.UTF_8)).get()
        val getMessagesJSON = JSONObject(
            mutableMapOf(
                Pair("connectionId", "1234-1234-1234-1234"),
                Pair("timestamp", timestamp),
                Pair("timestampSig", Base64.getEncoder().encodeToString(timestampSig))
            ) as Map<*, *>
        )
        postToLocalTest(getMessagesJSON.toString(), "getMessages")
    }

    private suspend fun postToLocalTest(data: String, action: String) {
        val postRequest = Request.Builder()
            .url("http://localhost:3000/$action")
            .post(data.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()
        val client = OkHttpClient()
        val response = withContext(Dispatchers.IO) {
            client.newCall(postRequest).execute()
        }
    }

    private fun getFirstDidInWallet(): String {
        val myDids = Did.getListMyDidsWithMeta(openWallet).get()
        val myDidJSON = JSONObject("{ \"dids\": $myDids}").getJSONArray("dids").getJSONObject(0)
        println(myDidJSON)
        val did = myDidJSON.get("did").toString()
        return did
    }

    @Test
    fun testAnchorDid() = runBlocking<Unit> {
        val endorserDid = getFirstDidInWallet()

        Did.createAndStoreMyDid(openWallet, "{}").get()

        Pool.createPoolLedgerConfig("BuilderNet",
            null
            ).get()

        Pool.openPoolLedger("BuilderNet", null).get()
    }
}