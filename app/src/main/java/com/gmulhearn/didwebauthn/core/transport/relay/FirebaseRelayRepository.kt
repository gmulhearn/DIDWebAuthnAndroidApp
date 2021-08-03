package com.gmulhearn.didwebauthn.core.transport.relay

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import javax.inject.Inject

class FirebaseRelayRepository @Inject constructor(
    context: Context
) : RelayRepository {

    private val didPostboxManager = DIDPostboxManager(context)

    // todo convert below to delegated
    private val app = FirebaseApp.initializeApp(context)!!
    private val firestore = FirebaseFirestore.getInstance(app)

    override fun getServiceEndpoint(did: String): String {
        val projectId = app.options.projectId!!

        val region = "us-central1"
        val functionName = "endpoint"

        val postboxID = didPostboxManager.getPostboxIDForDID(did)

        return "https://$region-$projectId.cloudfunctions.net/$functionName?p=$postboxID"
    }

    suspend fun readAllMessages(postboxID: String) {

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

    override fun initializePostbox(did: String) {
        TODO("Not yet implemented")
    }

    override fun subscribeToMessages(did: String, onReceiveMessage: (ByteArray) -> Unit) {
        val postboxID = didPostboxManager.getPostboxIDForDID(did)

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

    override fun getMessages(did: String): List<ByteArray> {
        TODO("Not yet implemented")
    }

    override fun storeMessage(did: String, message: String) {
        TODO("Not yet implemented")
    }
}