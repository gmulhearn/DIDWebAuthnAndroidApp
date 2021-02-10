package com.example.did

import org.junit.Test
import org.junit.Assert.*
import org.hyperledger.indy.sdk.*
import org.hyperledger.indy.sdk.crypto.Crypto
import org.hyperledger.indy.sdk.wallet.Wallet
import java.io.File
import org.hyperledger.indy.sdk.did.Did
import org.hyperledger.indy.sdk.pairwise.Pairwise
import org.json.JSONObject
import org.junit.Before

/**
 * Example local unit test, which will execute on the development machine (host).
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
        openWallet = Wallet.openWallet(config, credentials).get()

        val key2 = "EqZbeZJ8uhAxcurnKaeTMPKxEfaLZXaxnReCECApaABX"
        val credentials2 = "{\"key\":\"$key2\"}"
        val config2 = "{\"id\":\"testID2\"}"
        openWallet2 = Wallet.openWallet(config2, credentials2).get()
    }

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
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
    fun `pairwise`() {
        val myDid = Did.createAndStoreMyDid(openWallet, "{}").get()
        val theirDid = Did.createAndStoreMyDid(openWallet2, "{}").get()

        println(myDid)
        println(theirDid)

        Did.storeTheirDid(
            openWallet,
            "{\"did\":\"%s\",\"verkey\":\"%s\"}".format(theirDid.did, theirDid.verkey)
        ).get()

        Pairwise.createPairwise(openWallet, theirDid.did, myDid.did, null).get()
        println(Pairwise.listPairwise(openWallet).get())
    }

    @Test
    fun `messaging`() {
        val myDids = Did.getListMyDidsWithMeta(openWallet).get()
        val myDid = JSONObject("{ \"dids\": $myDids}").getJSONArray("dids").getJSONObject(0)
        val theirDids = Did.getListMyDidsWithMeta(openWallet2).get()
        val theirDid = JSONObject("{ \"dids\": $theirDids}").getJSONArray("dids").getJSONObject(0)

        val recipVKs = "[\"${theirDid.getString("verkey")}\"]"
        val encryptedMsg = Crypto.packMessage(
            openWallet,
            recipVKs,
            myDid.getString("verkey"),
            "hello world".toByteArray(Charsets.UTF_8)
        ).get()

        println(encryptedMsg.toString(Charsets.UTF_8))

        val decryptedMsg = Crypto.unpackMessage(openWallet2, encryptedMsg).get()
        println(decryptedMsg.toString(Charsets.UTF_8))
    }
}