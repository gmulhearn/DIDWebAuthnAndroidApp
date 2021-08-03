package com.gmulhearn.didwebauthn.core.transport.relay

import android.content.Context
import android.provider.Settings
import com.gmulhearn.didwebauthn.common.WalletProvider
import javax.inject.Inject

class DIDPostboxManager(
    private val context: Context
) {
    @Inject
    internal lateinit var walletProvider: WalletProvider

    fun getPostboxIDForDID(did: String): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun storePostboxIDForDID(postboxID: String, did: String) {

    }
}