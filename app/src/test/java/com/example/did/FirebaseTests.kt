package com.example.did

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

}