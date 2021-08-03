package com.gmulhearn.didwebauthn.transport.relay

import android.content.Context
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
import javax.inject.Inject
import kotlin.reflect.KFunction1

class FirebaseRelayRepository @Inject constructor(
    context: Context
) : RelayRepository {
    // todo convert below to delegated
    private val app = FirebaseApp.initializeApp(context)!!
    private val firestore = FirebaseFirestore.getInstance(app)

    override fun getServiceEndpoint(postboxID: String): String {
        val projectId = app.options.projectId!!

        val region = "us-central1"
        val functionName = "endpoint"

        return "https://$region-$projectId.cloudfunctions.net/$functionName?p=$postboxID"
    }

    suspend fun readAllMessages(postboxID: String) {
//        val firestore = FirebaseFirestore.getInstance(app)

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

    override fun initializePostbox(postboxID: String) {
        TODO("Not yet implemented")
    }

    override fun subscribeToMessages(postboxID: String, onReceiveMessage: (ByteArray) -> Unit) {
        Firebase.auth.signInAnonymously().addOnCompleteListener {
            firestore
                .collection("postboxes")
                .document(postboxID)
                .collection("messages")
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    value?.documentChanges?.firstOrNull()?.document?.data?.let { firebaseMessage ->
                        val msgData = firebaseMessage["message"]?.let { it as Blob }
                        onReceiveMessage(
                            msgData?.toBytes() ?: byteArrayOf()
                        )
                    }
                }
        }
    }

    override fun getMessages(postboxID: String): List<ByteArray> {
        TODO("Not yet implemented")
    }

    override fun storeMessage(postboxID: String, message: String) {
        TODO("Not yet implemented")
    }
}