package com.example.did.ui.didcomm.AddContact

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.graphics.createBitmap
import com.example.did.common.MSCoroutineScope
import com.example.did.common.ObjectDelegate
import com.example.did.common.di.qualifier.DidInformation
import com.example.did.common.di.qualifier.WalletInformation
import com.example.did.data.*
import com.example.did.protocols.DIDExchange.generateEncryptedRequestMessage
import com.example.did.protocols.DIDExchange.generateInvitation
import com.example.did.protocols.DIDExchange.generateInvitationUrl
import com.example.did.transport.FirebaseRelay
import com.google.firebase.FirebaseApp
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hyperledger.indy.sdk.crypto.Crypto
import org.hyperledger.indy.sdk.wallet.Wallet
import org.json.JSONObject
import java.sql.Blob
import java.util.*
import javax.inject.Inject

/**
 * AddContact VIPER Interactor Implementation
 */
class AddContactInteractor @Inject constructor(
    internal val coroutineScope: MSCoroutineScope,
    @WalletInformation internal val walletInfo: WalletInfo,
    @DidInformation internal val didInfo: DidInfo,
    private val context: Context
) : AddContactContract.InteractorInput, CoroutineScope by coroutineScope {

    internal val outputDelegate = ObjectDelegate<AddContactContract.InteractorOutput>()
    internal val output by outputDelegate

    // QR code stuff
    private val qrCodeWriter = QRCodeWriter()
    internal val barcodeFormatQRCode = BarcodeFormat.QR_CODE

    internal var wallet: Wallet? = null

    // region viper lifecycle

    override fun attachOutput(output: AddContactContract.InteractorOutput) {
        outputDelegate.attach(output)
    }

    override fun detachOutput() {
        coroutineScope.cancelJobs()
        outputDelegate.detach()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun generateQR() {
        val width = 200
        val height = 200
        val imageBitmap = createBitmap(width, height)

        println("GENERATING QR")
        launch {
            withContext(Dispatchers.IO) {

                if (wallet == null) {
                    openWallet()
                }

                try {
                    val hintMap: MutableMap<EncodeHintType, Any> =
                        EnumMap(EncodeHintType::class.java)
                    hintMap[EncodeHintType.MARGIN] = 0

                    val invite = generateInvitation(wallet!!, didInfo, context, "test-label")

                    val inviteUrl = generateInvitationUrl(invite)

                    val bitmapMatrix =
                        qrCodeWriter.encode(inviteUrl, barcodeFormatQRCode, width, height, hintMap)

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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun processQrScan(text: String?) {

        val inviteEncoded = text?.split("?c_i=")?.last()?.removeSuffix("%3D")
        println(inviteEncoded)
        var invite = ""
        try {
            invite = Base64.getDecoder().decode(inviteEncoded).toString(Charsets.UTF_8)
            println(invite)

            val invitationObj = Gson().fromJson(invite, Invitation::class.java)
            launch {
                replyToInvitation(invitationObj)
            }
        } catch (e: java.lang.Exception) {
            println(e)
            return
        }
    }

    private suspend fun replyToInvitation(invitation: Invitation) {
        val theirDid = DidInfo("unnecessary", invitation.recipientKeys.first())
        val replyEncrypted =
            generateEncryptedRequestMessage("android-device-todo", wallet!!, didInfo, theirDid, context)
        val firebase = FirebaseRelay(FirebaseApp.initializeApp(context)!!)
        firebase.transmitData(replyEncrypted, invitation.serviceEndpoint)
    }

    override fun loadData(savedState: Bundle?) {
        val firebase = FirebaseRelay(FirebaseApp.initializeApp(context)!!)
        val androidId =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        launch {
            firebase.waitForMessage(androidId, ::onMessage)
        }

    }

    private fun onMessage(data: Map<String, Any?>) {
        if (wallet == null) {
            openWallet()
        }
        println("onMessage: $data")
        val message = data["message"] as com.google.firebase.firestore.Blob
        var didCommMessage: DIDCommMessage
        try {
            val unencryptedMsg =
                Crypto.unpackMessage(wallet!!, message.toBytes()).get().toString(Charsets.UTF_8)
            println("unencrypted: $unencryptedMsg")

            didCommMessage = Gson().fromJson(unencryptedMsg, DIDCommMessage::class.java)
        } catch (e: java.lang.Exception) {
            println("failed to decrypt: $e")
            return
        }

        // TODO: this won't work android to android bcus it's not wrapped in a DIDCommMessage

        // Try as request
        try {
            val request = Gson().fromJson(didCommMessage.message.toString(), DIDRequestMessage::class.java)
            checkNotNull(request.connection)
            handleRequest(request)
            return
        } catch (e: java.lang.Exception) {
            println("faile to decode as request $e")
        }

        // Try as response
        try {
            val response = Gson().fromJson(didCommMessage.message.toString(), DIDResponseMessage::class.java)
            checkNotNull(response.connectionSig)
            handleResponse(response)
        } catch (e: java.lang.Exception) {
            println("faile to decode as response $e")
        }
    }

    private fun handleRequest(didRequest: DIDRequestMessage) {
        println(didRequest)
    }

    private fun handleResponse(didResponse: DIDResponseMessage) {
        println(didResponse)
    }

    override fun savePendingState(outState: Bundle) {
        // TODO save interactor state to bundle and output success if required
    }

    private fun openWallet() {
        wallet = Wallet.openWallet(walletInfo.config, walletInfo.credentials).get()
        println("OPENED WALLET")
    }

    // endregion

    // region interactor inputs


    // endregion
}
