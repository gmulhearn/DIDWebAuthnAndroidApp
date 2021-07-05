package com.gmulhearn.didwebauthn.ui.wallets

/**
 * Wallets display nodels, data models and errors for the module
 */
interface WalletsModels {

    // region View display models

    data class WalletDisplayModel(
        val walletID: String
    )

    // endregion

    // region Interactor Output data models and errors

    // endregion

}
