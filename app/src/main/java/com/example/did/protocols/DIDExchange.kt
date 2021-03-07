package com.example.did.protocols

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.example.did.data.DidInfo
import com.example.did.data.Invitation
import com.example.did.data.WalletInfo
import com.example.did.transport.FirebaseRelay
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import org.hyperledger.indy.sdk.did.Did
import org.hyperledger.indy.sdk.wallet.Wallet
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder
import java.util.*

object DIDExchange {
    @SuppressLint("HardwareIds")
    fun generateInvitation(
        wallet: Wallet,
        did: DidInfo,
        context: Context,
        label: String
    ): Invitation {
        val firebaseRelay = FirebaseRelay(FirebaseApp.initializeApp(context)!!)

        val didKey = Did.keyForLocalDid(wallet, did.did).get()
        val androidId =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        val endpoint = firebaseRelay.getServiceEndpoint(androidId)

        return Invitation(
            id = androidId,
            label = label,
            recipientKeys = listOf(didKey),
            serviceEndpoint = endpoint,
            routingKeys = listOf()
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateInvitationUrl(invitation: Invitation): String {
        val baseUrl = "https://ssisample.sudoplatform.com/?c_i="
        val inviteJsonString = invitation.toJsonString()
        println(inviteJsonString)
        val encodedInvite =
            Base64.getEncoder().encode(inviteJsonString.toByteArray(Charsets.UTF_8)).toString(Charsets.UTF_8)

        return baseUrl + encodedInvite
    }
}

fun Invitation.toJsonString(): String {
    return Gson().toJson(this)
}