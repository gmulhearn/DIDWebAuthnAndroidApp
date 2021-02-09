package com.example.did.ui.dids

/**
 * DIDs display nodels, data models and errors for the module
 */
interface DIDsModels {

    // region View display models

    data class DidDisplayModel(
        val did: String,
        val verkey: String
    )

    // endregion

    // region Interactor Output data models and errors

    // endregion

}
