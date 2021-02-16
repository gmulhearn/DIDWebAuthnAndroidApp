package com.example.did.ui.didcomm.AddContact

import android.graphics.Bitmap
import android.os.Bundle
import com.example.did.common.ObjectDelegate
import javax.inject.Inject

/**
 * AddContact VIPER Presenter Implementation
 */
class AddContactPresenter @Inject constructor(
        private val interactor: AddContactContract.InteractorInput,
        private val router: AddContactContract.Router
) : AddContactContract.Presenter, AddContactContract.InteractorOutput {

    internal val viewDelegate = ObjectDelegate<AddContactContract.View>()
    internal val view by viewDelegate

    // region viper lifecycle

    override fun attachView(view: AddContactContract.View) {
        viewDelegate.attach(view)
        interactor.attachOutput(this)
    }

    override fun detachView() {
        interactor.detachOutput()
        viewDelegate.detach()
    }

    override fun viewLoaded(savedState: Bundle?) {
        interactor.loadData(savedState)
        interactor.generateQR()
    }

    override fun saveState(outState: Bundle) {
        interactor.savePendingState(outState)
    }

    override fun qrCodeRead(text: String?) {
        TODO("Not yet implemented")
    }

    // endregion

    // region view event handlers

    // TODO Add view event handlers

    // endregion

    // region interactor output

    override fun loadDataResult() {
        // TODO handle result
    }

    override fun generatedQR(bitmap: Bitmap) {
        view.showQR(bitmap)
    }

    // TODO Add interactor outputs

    // endregion

}
