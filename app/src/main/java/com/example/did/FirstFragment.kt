package com.example.did

import android.os.Build
import android.os.Bundle
import android.system.Os
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import org.hyperledger.indy.sdk.LibIndy

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        Os.setenv("EXTERNAL_STORAGE", context?.filesDir?.absolutePath, true)
        println("external storage: ${Os.getenv("EXTERNAL_STORAGE")}")
        System.loadLibrary("indy")
        println("LOADED LIBRARY INDY")
        LibIndy.init()
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.button_first).setOnClickListener {
//            val key = Wallet.generateWalletKey(null).get()
//            println(key)
//
//            val credentials = "{\"key\":\"$key\"}"
//            val config = "{\"id\":\"testID2\"}"
//
//            Wallet.createWallet(config, credentials).get()
//
////            val credentials = "{\"key\":\"$key\"}"
////            val config = "{\"id\":\"testID1\"}"
//            val wallet = Wallet.openWallet(config, credentials).get()
//
//            val did = Did.createAndStoreMyDid(wallet, "{}").get()
//
//            println("\ndid: ${did.did}\n\nverkey: ${did.verkey}\n")
//
//            val message = "hello world"
//
//            val signature = Crypto.cryptoSign(wallet, did.verkey, message.toByteArray(Charsets.UTF_8)).get()
//
//            println("\"$message\" signature: ${signature.asList()}")
//
//            val verify = Crypto.cryptoVerify(did.verkey, message.toByteArray(Charsets.UTF_8), signature).get()
//
//            println("verify message \"$message\": $verify")
//
//            val badVerify = Crypto.cryptoVerify(did.verkey, "fake message".toByteArray(Charsets.UTF_8), signature).get()
//
//            println("verify message \"fake message\": $badVerify")
            findNavController().navigate(R.id.action_FirstFragment_to_WalletsFragment)
            // findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }
}