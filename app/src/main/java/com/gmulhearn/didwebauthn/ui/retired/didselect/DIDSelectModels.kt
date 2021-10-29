package com.gmulhearn.didwebauthn.ui.retired.didselect

/**
 * DIDSelect display nodels, data models and errors for the module
 */
interface DIDSelectModels {

    // region View display models

    data class DidDisplayModel(
        val did: String,
        val verkey: String
    )

    // endregion

    // region Interactor Output data models and errors

    // endregion

}
