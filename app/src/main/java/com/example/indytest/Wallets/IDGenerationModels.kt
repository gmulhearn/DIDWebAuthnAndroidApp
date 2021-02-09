package com.example.indytest.Wallets

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * IDGeneration display nodels, data models and errors for the module
 */
interface IDGenerationModels {

    // region View display models

    @Parcelize
    data class DidInfo(val did: String, val verkey: String) : Parcelable

    @Parcelize
    data class WalletInfo(val config: String, val credentials : String) : Parcelable

    data class WalletDisplayModel(
        val walletID: String
    )

    // endregion

    // region Interactor Output data models and errors

    // endregion

}
