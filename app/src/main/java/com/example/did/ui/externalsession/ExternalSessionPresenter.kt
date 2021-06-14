package com.example.did.ui.externalsession

import android.os.Bundle
import com.example.did.common.ObjectDelegate
import javax.inject.Inject

/**
 * ExternalSession VIPER Presenter Implementation
 */
class ExternalSessionPresenter @Inject constructor(
        private val interactor: ExternalSessionContract.InteractorInput,
        private val router: ExternalSessionContract.Router
) : ExternalSessionContract.Presenter, ExternalSessionContract.InteractorOutput {

    internal val viewDelegate = ObjectDelegate<ExternalSessionContract.View>()
    internal val view by viewDelegate

    // region viper lifecycle

    override fun attachView(view: ExternalSessionContract.View) {
        viewDelegate.attach(view)
        interactor.attachOutput(this)
    }

    override fun detachView() {
        interactor.detachOutput()
        viewDelegate.detach()
    }

    override fun viewLoaded(savedState: Bundle?) {
        interactor.loadData(savedState)
        view.setupCamera()
    }

    override fun saveState(outState: Bundle) {
        interactor.savePendingState(outState)
    }

    override fun qrCodeRead(data: String) {
        interactor.processQrScan(data)
    }

    override fun onClientSignalled(data: String) {
        interactor.processClientSignal(data)
    }

    // endregion

    // region view event handlers

    // TODO Add view event handlers

    // endregion

    // region interactor output

    override fun loadDataResult() {
        // TODO handle result
    }

    override fun retrievedSignal(sig: String) {
        view.signalClient(sig)
    }

    override fun connectionSuccess() {
        view.hideCamera()
        view.showConnected()
    }

    // TODO Add interactor outputs

    // endregion

}
