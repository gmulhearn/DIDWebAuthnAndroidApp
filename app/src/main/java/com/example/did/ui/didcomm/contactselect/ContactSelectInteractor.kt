package com.example.did.ui.didcomm.contactselect

import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.Color
import android.os.Bundle
import androidx.core.graphics.createBitmap
import com.example.did.common.MSCoroutineScope
import com.example.did.common.ObjectDelegate
import com.example.did.common.di.qualifier.DidInformation
import com.example.did.common.di.qualifier.WalletInformation
import com.example.did.data.DidInfo
import com.example.did.data.WalletInfo
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.CoroutineScope
import java.util.*
import javax.inject.Inject

/**
 * ContactSelect VIPER Interactor Implementation
 */
class ContactSelectInteractor @Inject constructor(
    internal val coroutineScope: MSCoroutineScope,
    @WalletInformation internal val walletInfo: WalletInfo,
    @DidInformation internal val didInfo: DidInfo,
    internal val router: ContactSelectRouter
) : ContactSelectContract.InteractorInput, CoroutineScope by coroutineScope {
    
    internal val outputDelegate = ObjectDelegate<ContactSelectContract.InteractorOutput>()
    internal val output by outputDelegate


    // region viper lifecycle

    override fun attachOutput(output: ContactSelectContract.InteractorOutput) {
        outputDelegate.attach(output)
    }
    
    override fun detachOutput() {
        coroutineScope.cancelJobs()
        outputDelegate.detach()
    }

    override fun loadData(savedState: Bundle?) {
        // TODO implement this. Call output with results of a data load or load existing state
    }

    override fun savePendingState(outState: Bundle) {
        // TODO save interactor state to bundle and output success if required
    }

    override fun toAddContact() {
        router.toAddContact(didInfo, walletInfo)
    }

    // endregion
    
    // region interactor inputs


    // endregion
}
