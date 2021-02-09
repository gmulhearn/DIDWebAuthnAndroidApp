package com.example.indytest.DIDs

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * DIDs display nodels, data models and errors for the module
 */
interface DIDsModels {

    // region View display models

    @Parcelize
    data class DidInfo(val did: String, val verkey: String) : Parcelable

    // endregion

    // region Interactor Output data models and errors

    // endregion

}
