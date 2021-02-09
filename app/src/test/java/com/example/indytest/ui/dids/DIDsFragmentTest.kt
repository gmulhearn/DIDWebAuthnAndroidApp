package com.example.indytest.ui.dids

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.navigation.toNavBundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.anonyome.sudotestfoundation.BaseTests
import com.nhaarman.mockitokotlin2.*
import io.kotlintest.shouldNotBe
import io.kotlintest.shouldBe
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import com.anonyome.mysudo.R

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class)
class DIDsFragmentTest : BaseTests() {

    private val presenter by before { mock<DIDsPresenter>() }

    private val fragment by before {
        spy(DIDsFragment()) {
            // Stop fragment from injecting with dagger
            doNothing().whenever(it).inject()
        }.apply {
            // Set any injectable values
            presenter = this@DIDsFragmentTest.presenter
        }
    }

    private val scenario by before {
        launchFragmentInContainer(themeResId = R.style.Theme_AppCompat, instantiate =  { fragment })
    }

    @Before
    fun setup() {
        // Verify the fragment is injected and that the presenter is assigned.
        verify(fragment).inject()
        verify(presenter).attachView(eq(fragment))
        verify(presenter).viewLoaded(anyOrNull())
        fragment.presenter shouldNotBe null
    }

    @After
    fun tearDown() {
        // Catch all unverified interactions
        verifyNoMoreInteractions(presenter)
    }

    @Test
    fun `VIPER detaches from fragment`() {
        scenario.moveToState(Lifecycle.State.DESTROYED)

        // Verify that the presenter is assigned.
        verify(presenter).detachView()
    }

    @Test
    fun `fragment robolectric tests required`() {
        Assert.fail("Fragment unit tests have not been implemented for DIDs. Replace this method with actual tests.")
    }
}