package com.gmulhearn.didwebauthn.ui.externalsession

import android.os.Bundle
import com.gmulhearn.didwebauthn.common.ObjectDelegate
import com.gmulhearn.didwebauthn.core.protocols.getId
import com.gmulhearn.didwebauthn.data.AllowCredentialDescriptor
import com.gmulhearn.didwebauthn.data.UserInfo
import java.util.Base64
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

    override fun onServerMessage(data: String) {
        interactor.handleServerMessage(data)
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

    override fun responseGenerated(jsonData: String) {
        view.sendMessageInWebView(jsonData)
    }

    override fun requestUserRegistrationConfirmation(
        origin: String,
        userInfo: UserInfo,
        onConfirmation: () -> Unit
    ) {
        view.showUserPrompt(
            title = "Incoming WebAuthn Registration Request",
            message = "request from origin: $origin\nfor user: ${userInfo.displayName}",
            onConfirmation = onConfirmation
        )
    }

    override fun requestUserAuthenticationConfirmation(
        origin: String,
        allowedCredentials: List<AllowCredentialDescriptor>,
        onConfirmation: () -> Unit
    ) {
        val allowedCredsString = allowedCredentials.joinToString(separator = ",\n", prefix = "- ") { it.getId().toString(Charsets.UTF_8) }
        view.showUserPrompt(
            title = "Incoming WebAuthn Authentication Request",
            message = "request from origin: $origin\nallowed keys Ids: $allowedCredsString",
            onConfirmation = onConfirmation
        )
    }

    // TODO Add interactor outputs

    // endregion

}
