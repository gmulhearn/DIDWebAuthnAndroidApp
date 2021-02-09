package com.example.indytest.ui.signing

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

class SigningInteractorTest : BaseTests() {

    private val coroutineScopeSpy: MSCoroutineScope by before {
        spy(MSCoroutineScope(dispatchers = TestDispatcherProvider()))
    }

    private val interactor: SigningInteractor by before {
        SigningInteractor(coroutineScopeSpy)
    }

    private val output by before {
        mock<SigningContract.InteractorOutput>()
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
        Assert.fail("Interactor unit tests have not been implemented for Signing. Replace this method with actual tests.")
    }
}