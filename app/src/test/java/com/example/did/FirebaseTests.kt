package com.example.did

import com.example.did.data.Invitation
import com.example.did.protocols.toJsonString
import com.example.did.transport.FirebaseRelay
import com.example.did.ui.didselect.DIDSelectFragment
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FirebaseTests {

    private lateinit var firebaseRelay: FirebaseRelay

    @Before
    fun setup() {
        // FirebaseApp.initializeApp()
        // firebaseRelay = FirebaseRelay(FirebaseApp.getInstance())
    }

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun JSONInviteTest() {
        val invite = Invitation(
            "did:sov:1q23r12241", "aigisdoih12", "test device",
            listOf("abcde123acde"), "endpoint.com/abcde123acde", listOf()
        )

        println(invite.toJsonString())
    }

}