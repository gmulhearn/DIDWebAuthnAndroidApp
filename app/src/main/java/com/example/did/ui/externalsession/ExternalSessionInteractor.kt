package com.example.did.ui.externalsession

import android.os.Bundle
import com.example.did.common.MSCoroutineScope
import com.example.did.common.ObjectDelegate
import com.squareup.okhttp.MediaType
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.RequestBody
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject

/**
 * ExternalSession VIPER Interactor Implementation
 */
class ExternalSessionInteractor @Inject constructor(
    internal val coroutineScope: MSCoroutineScope
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
        val serverSig = JSONObject(resStr).getJSONObject("Item").get("initSignal").toString()
        return serverSig
    }

    // endregion
    
    // region interactor inputs


    // endregion
}
