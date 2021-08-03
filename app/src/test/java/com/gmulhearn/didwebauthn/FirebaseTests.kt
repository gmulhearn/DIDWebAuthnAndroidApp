package com.gmulhearn.didwebauthn

import com.gmulhearn.didwebauthn.data.Invitation
import com.gmulhearn.didwebauthn.core.protocols.toJsonString
import com.gmulhearn.didwebauthn.core.transport.relay.FirebaseRelayRepository
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FirebaseTests {

    private lateinit var firebaseRelay: FirebaseRelayRepository

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