package com.example.did.transport

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.squareup.okhttp.MediaType
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.RequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import kotlin.reflect.KFunction1

class FirebaseRelay(app: FirebaseApp) {
    private val app = app

    fun getServiceEndpoint(id: String, projectIdA: String = ""): String {
        val projectId = app.options.projectId!!

        val region = "us-central1"
        val functionName = "endpoint"

        return "https://$region-$projectId.cloudfunctions.net/$functionName?p=$id"
    }

    suspend fun readAllMessages(postboxID: String) {
        val firestore = FirebaseFirestore.getInstance(app)

        Firebase.auth.signInAnonymously().addOnCompleteListener {
            firestore
                .collection("postboxes")
                .document(postboxID)
                .collection("messages")
                .get()
                .addOnCompleteListener {
                    it.result?.documents?.forEach {
                        val blob = it.data?.get("message") as Blob
                        println(blob.toBytes().toString(Charsets.UTF_8))
                    }
                }
        }
    }

    suspend fun waitForMessage(
        postboxID: String, onComplete: KFunction1<@ParameterName(
            name = "data"
        ) Map<String, Any?>, Unit>
    ) {
        val firestore = FirebaseFirestore.getInstance(app)

        Firebase.auth.signInAnonymously().addOnCompleteListener {
            firestore
                .collection("postboxes")
                .document(postboxID)
                .collection("messages")
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    value?.documentChanges?.firstOrNull()?.document?.data?.let { it1 -> onComplete(it1) }
                }
        }
    }

    suspend fun transmitData(data: ByteArray, endpoint: String): Boolean {
        println("TRANSMITTING DATA: ${data.toString(Charsets.UTF_8)}")
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