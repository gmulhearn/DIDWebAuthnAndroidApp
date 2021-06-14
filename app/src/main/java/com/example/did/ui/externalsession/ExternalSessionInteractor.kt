package com.example.did.ui.externalsession

import android.content.Context
import android.os.Bundle
import com.example.did.common.MSCoroutineScope
import com.example.did.common.ObjectDelegate
import com.example.did.common.WalletProvider
import com.example.did.data.*
import com.example.did.data.WebRTCMessageTypes.WEBAUTHN_AUTH_REQUEST
import com.example.did.data.WebRTCMessageTypes.WEBAUTHN_AUTH_RESPONSE
import com.example.did.data.WebRTCMessageTypes.WEBAUTHN_REG_REQUEST
import com.example.did.protocols.DIDAuthenticator
import com.example.did.protocols.JSON
import com.example.did.protocols.getChallenge
import com.example.did.protocols.toAuthenticatorGetAssertionOptions
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.squareup.okhttp.MediaType
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.RequestBody
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.*
import javax.inject.Inject

/**
 * ExternalSession VIPER Interactor Implementation
 */
class ExternalSessionInteractor @Inject constructor(
    internal val coroutineScope: MSCoroutineScope,
    internal val walletProvider: WalletProvider,
    internal val context: Context
) : ExternalSessionContract.InteractorInput, CoroutineScope by coroutineScope {
    
    internal val outputDelegate = ObjectDelegate<ExternalSessionContract.InteractorOutput>()
    internal val output by outputDelegate

    internal var serverNonce: Int = -1

    companion object {
        const val GET_INIT_SIG_ENDPOINT = "https://xl8dewabb6.execute-api.ap-southeast-2.amazonaws.com/default/readSignal"
        const val UPDATE_ENDPOINT = "https://xl8dewabb6.execute-api.ap-southeast-2.amazonaws.com/default/replySignal"
    }
    
    // region viper lifecycle

    override fun attachOutput(output: ExternalSessionContract.InteractorOutput) {
        outputDelegate.attach(output)
    }
    
    override fun detachOutput() {
        coroutineScope.cancelJobs()
        outputDelegate.detach()
    }

    override fun loadData(savedState: Bundle?) {
        // TODO implement this. Call output with results of a data load or load existing state
    }

    override fun savePendingState(outState: Bundle) {
        // TODO save interactor state to bundle and output success if required
    }

    override fun processQrScan(data: String) {
        launch {
            serverNonce = data.toInt()
            val sig = getInitSignal(serverNonce)
            output.retrievedSignal(sig)
        }
    }

    override fun processClientSignal(data: String) {
        launch {
            replyToSignal(serverNonce, data)
            // todo - check if success...
            output.connectionSuccess()
        }
    }

    override fun handleServerMessage(data: String) {
        val wrtcBaseMessageIn: WRTCBaseMessageIn
        try {
            wrtcBaseMessageIn = Gson().fromJson(data, WRTCBaseMessageIn::class.java)
        } catch (e: JsonSyntaxException) {
            return
        }

        when (wrtcBaseMessageIn.type) {
            WEBAUTHN_REG_REQUEST -> {}
            WEBAUTHN_AUTH_REQUEST -> {
                println("server sent auth request")
                val opts = Gson().fromJson(wrtcBaseMessageIn.jsonData, PublicKeyCredentialRequestOptions::class.java)
                println(opts)
                handleWebAuthnAuthentication(opts)
            }
            else -> {println("unknown wrtc message: $data : $wrtcBaseMessageIn")}
        }
    }

    private fun handleWebAuthnAuthentication(opts: PublicKeyCredentialRequestOptions) {
        val authenticator = DIDAuthenticator(context, walletProvider)

        val origin = "https://webauthn.io/" // TODO

        val getAssertionOpts = opts.publicKey.toAuthenticatorGetAssertionOptions(origin)
        val clientData = CollectedClientData(
            type = "webauthn.get",
            challengeBase64URL = Base64.getUrlEncoder().encodeToString(opts.publicKey.getChallenge()).removeSuffix("="),
            origin = origin
        )

        val response = authenticator.getAssertion(
            getAssertionOpts,
            clientData.JSON()
        )

        println(response)

        val webRTCResponse = wrapToWebRTCMessageOut(WEBAUTHN_AUTH_RESPONSE, response)

        output.responseGenerated(Gson().toJson(webRTCResponse))
    }

    private fun wrapToWebRTCMessageOut(type: String, data: Any): WRTCBaseMessageOut {
        return WRTCBaseMessageOut(type, data)
    }

    private suspend fun replyToSignal(nonce: Int, data: String) {
        println("replying...")
        val postRequest = Request.Builder()
            .url(UPDATE_ENDPOINT)
            .post(RequestBody.create(MediaType.parse("application/json"), """{"nonce": $nonce, "resSignal": $data}"""))
            .build()

        val client = OkHttpClient()
        val response = withContext(Dispatchers.IO) {
            client.newCall(postRequest).execute()
        }
        println(response)
        println(response.body().string())
    }

    private suspend fun getInitSignal(nonce: Int): String {
        val postRequest = Request.Builder()
            .url(GET_INIT_SIG_ENDPOINT)
            .post(RequestBody.create(MediaType.parse("application/json"), """{"nonce": $nonce}"""))
            .build()

        val client = OkHttpClient()
        val response = withContext(Dispatchers.IO) {
            client.newCall(postRequest).execute()
        }
        val resStr = response.body().string()
        return JSONObject(resStr).getJSONObject("Item").get("initSignal").toString()
    }

    // endregion
    
    // region interactor inputs


    // endregion
}
