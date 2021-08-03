package com.gmulhearn.didwebauthn.core.transport.relay

import com.squareup.okhttp.MediaType
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.RequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


interface RelayRepository {
    fun initializePostbox(did: String)

    fun getServiceEndpoint(did: String): String

    fun subscribeToMessages(did: String, onReceiveMessage: (ByteArray) -> Unit)

    fun getMessages(did: String): List<ByteArray>

    fun storeMessage(did: String, message: String)

    suspend fun sendDataToEndpoint(data: ByteArray, endpoint: String): Boolean {
        val postRequest = Request.Builder()
            .url(endpoint)
            .post(RequestBody.create(MediaType.parse("application/ssi-agent-wire"), data))
            .build()

        val client = OkHttpClient()
        val response = withContext(Dispatchers.IO) {
            client.newCall(postRequest).execute()
        }
        println(response)
        return true
    }
}