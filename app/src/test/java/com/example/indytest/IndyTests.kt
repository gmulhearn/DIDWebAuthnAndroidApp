package com.example.indytest

import org.junit.Test
import org.junit.Assert.*
import org.hyperledger.indy.sdk.*
import org.hyperledger.indy.sdk.crypto.Crypto
import org.hyperledger.indy.sdk.wallet.Wallet
import org.hyperledger.indy.sdk.wallet.WalletAccessFailedException
import org.hyperledger.indy.sdk.wallet.WalletResults
import java.io.File
import org.hyperledger.indy.sdk.did.Did

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class IndyTests {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun `gen wallet`() {
        val config = "{\"id\":\"testID\"}"
        val key = Wallet.generateWalletKey(null).get()
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

        Wallet.exportWallet(wallet, "{\"path\":\"/Users/gmulhearne/Documents/indyWalletTest\",\"key\":\"password\"}").get()

        val did = Did.createAndStoreMyDid(wallet, "{}").get()

        println("\ndid: ${did.did}\n\nverkey: ${did.verkey}\n")

        val message = "hello world"

        val signature = Crypto.cryptoSign(wallet, did.verkey, message.toByteArray(Charsets.UTF_8)).get()

        println("\"$message\" signature: ${signature.asList()}")

        val verify = Crypto.cryptoVerify(did.verkey, message.toByteArray(Charsets.UTF_8), signature).get()

        println("verify message \"$message\": $verify")

        val badVerify = Crypto.cryptoVerify(did.verkey, "fake message".toByteArray(Charsets.UTF_8), signature).get()

        println("verify message \"fake message\": $badVerify")
    }
}