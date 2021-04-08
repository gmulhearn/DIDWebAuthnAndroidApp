package com.example.did.ui.browser

import com.anonyome.sudotestfoundation.BaseTests
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.After
import org.junit.Assert
import org.junit.Test

class BrowserRouterTest : BaseTests() {

    private val router by before {
        BrowserRouter()
    }

    @After
    fun tearDown() {
        // Catch all unverified interactions.
        // verifyNoMoreInteractions() // TODO: Add any other mocks here and uncomment line
    }

    @Test
    fun `router test required`() {
        Assert.fail("Router unit tests have not been implemented for Browser. Replace this method with actual tests.")
    }
}
