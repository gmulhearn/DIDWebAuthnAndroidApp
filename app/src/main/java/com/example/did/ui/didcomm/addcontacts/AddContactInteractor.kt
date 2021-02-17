package com.example.did.ui.didcomm.AddContact

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
 * AddContact VIPER Interactor Implementation
 */
class AddContactInteractor @Inject constructor(
    internal val coroutineScope: MSCoroutineScope,
    @WalletInformation internal val walletInfo: WalletInfo,
    @DidInformation internal val didInfo: DidInfo
) : AddContactContract.InteractorInput, CoroutineScope by coroutineScope {
    
    internal val outputDelegate = ObjectDelegate<AddContactContract.InteractorOutput>()
    internal val output by outputDelegate

    // QR code stuff
    private val qrCodeWriter = QRCodeWriter()
    internal val barcodeFormatQRCode = BarcodeFormat.QR_CODE

    // region viper lifecycle

    override fun attachOutput(output: AddContactContract.InteractorOutput) {
        outputDelegate.attach(output)
    }
    
    override fun detachOutput() {
        coroutineScope.cancelJobs()
        outputDelegate.detach()
    }

    override fun generateQR() {
        val width = 200
        val height = 200
        val imageBitmap = createBitmap(width, height)

        try {
            val hintMap: MutableMap<EncodeHintType, Any> = EnumMap(EncodeHintType::class.java)
            hintMap[EncodeHintType.MARGIN] = 0

            val bitmapMatrix = qrCodeWriter.encode(didInfo.toString(), barcodeFormatQRCode, width, height, hintMap)

            for (i in 0 until width) {
                for (j in 0 until height) {
                    imageBitmap.setPixel(i, j, if (bitmapMatrix.get(i, j)) Color.BLACK else Color.WHITE)
                }
            }
        } catch (error: Throwable) {

        }

        output.generatedQR(imageBitmap)
    }

    override fun processQrScan(text: String?) {
        println(text)
    }

    override fun loadData(savedState: Bundle?) {
        // TODO implement this. Call output with results of a data load or load existing state
    }

    override fun savePendingState(outState: Bundle) {
        // TODO save interactor state to bundle and output success if required
    }

    // endregion
    
    // region interactor inputs


    // endregion
}
