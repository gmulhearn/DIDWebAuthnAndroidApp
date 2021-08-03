package com.gmulhearn.didwebauthn.core.transport.relay

import android.content.Context
import com.gmulhearn.didwebauthn.common.WalletProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class FirebaseRelayRepository @Inject constructor(
    context: Context,
    private val walletProvider: WalletProvider
) : RelayRepository {

    private val didPostboxManager = DIDPostboxManager(walletProvider)

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

    override fun initializePostbox(did: String) {
        if (!didPostboxManager.checkDIDPostboxExists(did)) {
            val newPostboxID = UUID.randomUUID().toString()
            didPostboxManager.storePostboxIDForDID(newPostboxID, did)
        }
    }

    override fun subscribeToMessages(did: String, onReceiveMessage: (ByteArray) -> Unit) {
        val postboxID = didPostboxManager.getPostboxIDForDID(did)

        Firebase.auth.signInAnonymously().addOnCompleteListener {
            firestore.collection("postboxes")
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

    @ExperimentalCoroutinesApi
    override suspend fun getMessages(did: String): List<ByteArray> {
        val postboxID = didPostboxManager.getPostboxIDForDID(did)

        return suspendCancellableCoroutine { cont ->
            Firebase.auth.signInAnonymously().addOnCompleteListener {
                firestore
                    .collection("postboxes")
                    .document(postboxID)
                    .collection("messages")
                    .get()
                    .addOnCompleteListener { task ->
                        // return this:
                        cont.resume(
                            value = task.result?.documents?.sortedBy { doc ->
                                (doc.data?.get("createdAt") as Timestamp).seconds
                            }?.map { doc ->
                                val blob = doc.data?.get("message") as Blob
                                blob.toBytes()
                            } ?: emptyList(),
                            onCancellation = {}
                        )
                    }
            }
        }
    }

    override suspend fun storeMessage(did: String, data: ByteArray) {

        val postboxID = didPostboxManager.getPostboxIDForDID(did)

        Firebase.auth.signInAnonymously().addOnCompleteListener {
            firestore
                .collection("postboxes")
                .document(postboxID)
                .collection("messages")
                .add(hashMapOf("createdAt" to Timestamp(Date(Date().time - 1000)), "message" to Blob.fromBytes(data)))
        }
    }
}