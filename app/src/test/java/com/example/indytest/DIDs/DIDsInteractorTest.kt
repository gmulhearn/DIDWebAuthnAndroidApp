package com.example.indytest.DIDs

import com.anonyome.mysudo.base.MSCoroutineScope
import com.anonyome.sudotestfoundation.BaseTests
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import io.kotlintest.shouldBe
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class DIDsInteractorTest : BaseTests() {

    private val coroutineScopeSpy: MSCoroutineScope by before {
        spy(MSCoroutineScope(dispatchers = TestDispatcherProvider()))
    }

    private val interactor: DIDsInteractor by before {
        DIDsInteractor(coroutineScopeSpy)
    }

    private val output by before {
        mock<DIDsContract.InteractorOutput>()
    }

    @Before
    fun setup() {
        interactor.attachOutput(output)

        // Verify output attaches after interactor is attached
        interactor.outputDelegate.isAttached() shouldBe true
    }

    @After
    fun tearDown() {
        interactor.detachOutput()

        // Verify output detaches after interactor is detached and coroutine scope is cleaned up
        interactor.outputDelegate.isAttached() shouldBe false
        verify(interactor.coroutineScope).cancelJobs()

        // Catch all unverified interactions. Add any other mocks here
        verifyNoMoreInteractions(output)
    }

    @Test
    fun `interactor test required`() {
        Assert.fail("Interactor unit tests have not been implemented for DIDs. Replace this method with actual tests.")
    }
}