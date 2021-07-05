package com.gmulhearn.didwebauthn.ui.externalauth

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.graphics.createBitmap
import com.gmulhearn.didwebauthn.common.MSCoroutineScope
import com.gmulhearn.didwebauthn.common.ObjectDelegate
import com.gmulhearn.didwebauthn.common.WalletProvider
import com.gmulhearn.didwebauthn.data.*
import com.gmulhearn.didwebauthn.protocols.*
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

/**
 * ExternalAuth VIPER Interactor Implementation
 */
class ExternalAuthInteractor @Inject constructor(
    internal val coroutineScope: MSCoroutineScope,
    internal val walletProvider: WalletProvider,
    internal val context: Context
) : ExternalAuthContract.InteractorInput, CoroutineScope by coroutineScope {
    
    internal val outputDelegate = ObjectDelegate<ExternalAuthContract.InteractorOutput>()
    internal val output by outputDelegate

    // QR code stuff
    private val qrCodeWriter = QRCodeWriter()
    internal val barcodeFormatQRCode = BarcodeFormat.QR_CODE
    
    // region viper lifecycle

    override fun attachOutput(output: ExternalAuthContract.InteractorOutput) {
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateQR(data: String) {
        val width = 1000
        val height = 1000
        val imageBitmap = createBitmap(width, height)

        println("GENERATING QR")
        launch {
            withContext(Dispatchers.IO) {

                try {
                    val hintMap: MutableMap<EncodeHintType, Any> =
                        EnumMap(EncodeHintType::class.java)
                    hintMap[EncodeHintType.MARGIN] = 0

                    val bitmapMatrix =
                        qrCodeWriter.encode(data, barcodeFormatQRCode, width, height, hintMap)

                    for (i in 0 until width) {
                        for (j in 0 until height) {
                            imageBitmap.setPixel(
                                i,
                                j,
                                if (bitmapMatrix.get(i, j)) Color.BLACK else Color.WHITE
                            )
                        }
                    }
                } catch (error: Exception) {
                    println("EXCEPTION IN QR GENERATION: $error")
                }
            }
            output.generatedQR(imageBitmap)
        }
    }


    override fun processQrScan(text: String?) {
        println(text)

        // determine type of qr
        try {
            val opts = Gson().fromJson(text, PublicKeyCredentialCreationOptions::class.java)
            println(opts)
            if (opts.publicKey.user != null) { // TODO - better check
                generateQR(processCredCreate(opts).JSON())
            }
        } catch (e: Exception) { }
        try {
            val opts = Gson().fromJson(text, PublicKeyCredentialRequestOptions::class.java)
            println(opts)
            if (opts.publicKey.rpId != null) { // TODO - better check
                generateQR(processCredRequest(opts).base64JSON())
            }
        } catch (e: Exception) { }
    }

    private fun processCredCreate(opts: PublicKeyCredentialCreationOptions): PublicKeyCredentialAttestationResponse {
        val authenticator = DIDAuthenticator(context, walletProvider) // todo - delegate

        val makeCredOpts = opts.publicKey.toAuthenticatorMakeCredentialOptions("webauthn.io") // TODO!
        val clientData = CollectedClientData(
            type = "webauthn.create",
            challengeBase64URL = Base64.getUrlEncoder().encodeToString(opts.publicKey.getChallenge()).removeSuffix("="),
            origin = "https://webauthn.io/" // TODO
        )
        val response = authenticator.makeCredentials(
            makeCredOpts,
            clientData.JSON()
        )
        println(response)
        return response
    }

    private fun processCredRequest(opts: PublicKeyCredentialRequestOptions): PublicKeyCredentialAssertionResponse {
        val authenticator = DIDAuthenticator(context, walletProvider)  // todo - delegate

        val origin = "https://webauthn.io/" // TODO

        val getAssertionOpts = opts.publicKey.toAuthenticatorGetAssertionOptions(origin)
        val clientData = CollectedClientData(
            type = "webauthn.get",
            challengeBase64URL = Base64.getUrlEncoder().encodeToString(opts.publicKey.getChallenge()).removeSuffix("="),
            origin = origin
        )

        val response = authenticator.getAssertion(
            getAssertionOpts,
            clientData.JSON()
        )

        println(response)

        return response
    }

    // endregion
    
    // region interactor inputs


    // endregion
}
