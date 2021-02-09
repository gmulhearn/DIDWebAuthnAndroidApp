//package com.example.indytest.Wallets
//
//import com.anonyome.sudotestfoundation.BaseTests
//import com.nhaarman.mockitokotlin2.verify
//import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
//import io.kotlintest.shouldBe
//import com.nhaarman.mockitokotlin2.mock
//import org.junit.After
//import org.junit.Assert
//import org.junit.Before
//import org.junit.Test
//
//class WalletsPresenterTest: BaseTests() {
//
//    private val view: WalletsContract.View = mock()
//    private val interactor: WalletsContract.InteractorInput = mock()
//    private val router: WalletsContract.Router = mock()
//
//    private val presenter: WalletsPresenter by before{
//        WalletsPresenter(interactor, router)
//    }
//
//    @Before
//    fun setup() {
//        presenter.attachView(view)
//
//        // Verify interactor attaches after view is attached
//        presenter.viewDelegate.isAttached() shouldBe true
//        verify(interactor).attachOutput(presenter)
//    }
//
//    @After
//    fun tearDown() {
//        presenter.detachView()
//
//        // Verify interactor detaches after view is detached
//        presenter.viewDelegate.isAttached() shouldBe false
//        verify(interactor).detachOutput()
//
//        // Catch all unverified interactions. Add any other mocks here
//        verifyNoMoreInteractions(view, interactor, router)
//    }
//
//    @Test
//    fun `presenter test required`() {
//        Assert.fail("Presenter unit tests have not been implemented for Wallets. Replace this method with actual tests.")
//    }
//
//}