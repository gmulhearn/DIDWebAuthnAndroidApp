package com.gmulhearn.didwebauthn.ui.externalauth

import android.graphics.Bitmap
import android.os.Bundle
import com.gmulhearn.didwebauthn.common.ObjectDelegate
import javax.inject.Inject

/**
 * ExternalAuth VIPER Presenter Implementation
 */
class ExternalAuthPresenter @Inject constructor(
        private val interactor: ExternalAuthContract.InteractorInput,
        private val router: ExternalAuthContract.Router
) : ExternalAuthContract.Presenter, ExternalAuthContract.InteractorOutput {

    internal val viewDelegate = ObjectDelegate<ExternalAuthContract.View>()
    internal val view by viewDelegate

    // region viper lifecycle

    override fun attachView(view: ExternalAuthContract.View) {
        viewDelegate.attach(view)
        interactor.attachOutput(this)
    }

    override fun detachView() {
        interactor.detachOutput()
        viewDelegate.detach()
    }

    override fun viewLoaded(savedState: Bundle?) {
        interactor.loadData(savedState)
        // interactor.generateQR()
        view.setupCamera()
    }

    override fun saveState(outState: Bundle) {
        interactor.savePendingState(outState)
    }

    override fun qrCodeRead(text: String?) {
        interactor.processQrScan(text)
    }

    // endregion

    // region view event handlers

    // TODO Add view event handlers

    // endregion

    // region interactor output

    override fun loadDataResult() {
        // TODO handle result
    }

    override fun generatedQR(imageBitmap: Bitmap) {
        view.showQR(imageBitmap)
        view.hideCamera()
    }

    // TODO Add interactor outputs

    // endregion

}
