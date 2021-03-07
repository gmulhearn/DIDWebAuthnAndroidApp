package com.example.did.transport

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
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
                    value?.documentChanges?.first()?.document?.data?.let { it1 -> onComplete(it1) }
                }
        }
    }

    suspend fun transmitData(data: String, endpoint: String): Boolean {
        val endpointURL = URL(endpoint)

        val urlConnection = endpointURL.openConnection() as HttpURLConnection
        urlConnection.requestMethod = "POST"
        urlConnection.connectTimeout = 300000
        urlConnection.doOutput = true

        val requestData = data.toByteArray()

        urlConnection.setRequestProperty("charset", "utf-8")
        urlConnection.setRequestProperty("Content-length", requestData.size.toString())
        urlConnection.setRequestProperty("Content-Type", "application/ssi-agent-wire")

        val result: Boolean = withContext(Dispatchers.IO) {
            try {
                val outputStream = DataOutputStream(urlConnection.outputStream)
                outputStream.write(requestData)
                outputStream.flush()
            } catch (exception: Exception) {
                println("failed to write outstream $exception")
                return@withContext false
            }

            if (urlConnection.responseCode != HttpURLConnection.HTTP_OK
                && urlConnection.responseCode != HttpURLConnection.HTTP_ACCEPTED
                && urlConnection.responseCode != HttpURLConnection.HTTP_CREATED
            ) {
                try {
                    val inputStream = DataInputStream(urlConnection.inputStream)
                    val output = BufferedReader(InputStreamReader(inputStream)).readLine()

                    println(inputStream.toString())
                    println(urlConnection.responseMessage)
                    println(urlConnection.responseCode)
                    println()
                    println("http request failed: $output")
                    return@withContext false
                } catch (exception: Exception) {
                    println("errrrrr")
                    return@withContext false
                }
            } else {
                println("urlResponse ${urlConnection.responseCode}")
            }
            return@withContext true
        }

        return result
    }


}