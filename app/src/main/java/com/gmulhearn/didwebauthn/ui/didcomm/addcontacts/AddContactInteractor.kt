package com.gmulhearn.didwebauthn.ui.didcomm.addcontacts

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.graphics.createBitmap
import com.gmulhearn.didwebauthn.common.MSCoroutineScope
import com.gmulhearn.didwebauthn.common.ObjectDelegate
import com.gmulhearn.didwebauthn.common.WalletProvider
import com.gmulhearn.didwebauthn.common.di.qualifier.DidInformation
import com.gmulhearn.didwebauthn.data.*
import com.gmulhearn.didwebauthn.core.protocols.DIDCommProtocols
import com.gmulhearn.didwebauthn.core.transport.relay.RelayRepository
import com.gmulhearn.didwebauthn.data.indy.PairwiseData
import com.gmulhearn.didwebauthn.ui.didcomm.AddContact.AddContactContract
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.*
import org.hyperledger.indy.sdk.crypto.Crypto
import org.hyperledger.indy.sdk.did.Did
import org.hyperledger.indy.sdk.pairwise.Pairwise
import org.hyperledger.indy.sdk.wallet.Wallet
import java.util.*
import javax.inject.Inject

/**
 * AddContact VIPER Interactor Implementation
 */
class AddContactInteractor @Inject constructor(
    internal val coroutineScope: MSCoroutineScope,
    @DidInformation internal val didInfo: DidInfo,
    private val context: Context,
    private val walletProvider: WalletProvider,
    private val relay: RelayRepository
) : AddContactContract.InteractorInput, CoroutineScope by coroutineScope {

    private val didComm = DIDCommProtocols(relay)

    internal val outputDelegate = ObjectDelegate<AddContactContract.InteractorOutput>()
    internal val output by outputDelegate

    // QR code stuff
    private val qrCodeWriter = QRCodeWriter()
    internal val barcodeFormatQRCode = BarcodeFormat.QR_CODE

    internal var wallet: Wallet = walletProvider.getWallet()

    // TEMPORARY BELOW!!!
    internal var theirLabel = "todo-label"
    internal var theirDid = "todo"
    internal var theirVerkey = "todo"

    internal var myLabel = "Android Sample Device"

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

                try {
                    val hintMap: MutableMap<EncodeHintType, Any> =
                        EnumMap(EncodeHintType::class.java)
                    hintMap[EncodeHintType.MARGIN] = 0
                    val invite = didComm.generateInvitation(wallet, didInfo, myLabel)

                    val inviteUrl = didComm.generateInvitationUrl(invite)

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
        val inviteEncoded =
            text?.split("?c_i=")?.last()?.split("%")?.first()  // sometimes suffixed with %3D...
        println(inviteEncoded)
        var invite = ""
        try {
            invite = Base64.getUrlDecoder().decode(inviteEncoded).toString(Charsets.UTF_8)
            println(invite)

            val invitationObj = Gson().fromJson(invite, Invitation::class.java)
            launch {
                output.updateProtocolState(ProtocolStage.REPLYING)
                replyToInvitation(invitationObj)
                theirLabel = invitationObj.label
                theirVerkey = invitationObj.recipientKeys.first()
            }
        } catch (e: java.lang.Exception) {
            println(e)
            output.updateProtocolState(ProtocolStage.FAILED)
            return
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setLabel(label: String) {
        if (label.isNotBlank()) {
            myLabel = label
            generateQR()
        }
    }

    private suspend fun replyToInvitation(invitation: Invitation) {
        val theirDid = DidInfo("unnecessary", invitation.recipientKeys.first())
        val replyEncrypted =
            didComm.generateEncryptedRequestMessage(
                myLabel,
                wallet,
                didInfo,
                theirDid,
                invitation.routingKeys ?: listOf(),
                context
            )
        relay.sendDataToEndpoint(replyEncrypted, invitation.serviceEndpoint)
    }

    override fun loadData(savedState: Bundle?) {
        launch {
            relay.initializePostbox(didInfo.did)
            relay.subscribeToMessages(didInfo.did) { data -> processMessage(data) }
            generateQR()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun processMessage(data: ByteArray) {
        val didCommContainer: DIDCommContainer
        try {
            val unencryptedMsg =
                Crypto.unpackMessage(wallet, data).get().toString(Charsets.UTF_8)
            println("unencrypted: $unencryptedMsg")

            didCommContainer = Gson().fromJson(unencryptedMsg, DIDCommContainer::class.java)
        } catch (e: java.lang.Exception) {
            println("failed to decrypt: $e")
            return
        }

        // TODO: this won't work android to android bcus it's not wrapped in a DIDCommMessage

        // Try as request
        try {
            val request =
                Gson().fromJson(didCommContainer.message.toString(), DIDRequestMessage::class.java)
            checkNotNull(request.connection)
            output.updateProtocolState(ProtocolStage.RESPONDING)
            launch {
                handleRequest(request)
                output.updateProtocolState(ProtocolStage.SUCCESS) // todo: could be failed tho...
                delay(1000)
                output.onSuccessUpdate()
            }
            return
        } catch (e: java.lang.Exception) {
            println("failed to decode as request $e")
        }

        // Try as response
        try {
            val response =
                Gson().fromJson(didCommContainer.message.toString(), DIDResponseMessage::class.java)
            checkNotNull(response.connectionSig)
            output.updateProtocolState(ProtocolStage.PROCESSING_RESPONSE)
            handleResponse(response)
            output.updateProtocolState(ProtocolStage.SUCCESS)
            launch {
                delay(1000)
                output.onSuccessUpdate()
            }
        } catch (e: java.lang.Exception) {
            println("faile to decode as response $e")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun handleRequest(didRequest: DIDRequestMessage) {
        println(didRequest)

        val theirDid = DidInfo(
            didRequest.connection.did,
            didRequest.connection.didDoc.service.first().recipientKeys.first()
        )

        val encryptedResponseRaw = didComm.generateEncryptedResponseMessage(
            myWallet = wallet,
            myDid = didInfo,
            theirDid = theirDid,
            theirRoutingKeys = didRequest.connection.didDoc.service.first().routingKeys ?: listOf(),
            context = context
        )

        val result = withContext(Dispatchers.IO) {
            relay.sendDataToEndpoint(
                encryptedResponseRaw, // encryptedResponse,
                didRequest.connection.didDoc.service.first().serviceEndpoint
            )
        }
        if (result) {
            saveContactPairwise(
                theirDidInfo = theirDid,
                myDidInfo = didInfo,
                theirEndpoint = didRequest.connection.didDoc.service.first().serviceEndpoint,
                theirRoutingKeys = didRequest.connection.didDoc.service.first().routingKeys,
                label = didRequest.label
            )
        } else {
            // failed to send
            println("http error sending response")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleResponse(didResponse: DIDResponseMessage) {
        println("handle response: $didResponse")
        val connectionJson =
            Base64.getUrlDecoder().decode(didResponse.connectionSig.sigData)
                .toString(Charsets.UTF_8)
                .drop(8)
        val connection = Gson().fromJson(connectionJson, DIDRequestConnection::class.java)

        theirDid = connection.did

        saveContactPairwise(
            theirDidInfo = DidInfo(connection.did, theirVerkey),
            myDidInfo = didInfo,
            theirEndpoint = connection.didDoc.service.first().serviceEndpoint,
            theirRoutingKeys = connection.didDoc.service.first().routingKeys,
            label = theirLabel
        )
    }

    private fun saveContactPairwise(
        theirDidInfo: DidInfo,
        myDidInfo: DidInfo,
        theirEndpoint: String,
        theirRoutingKeys: List<String>?,
        label: String
    ) {
        Did.storeTheirDid(
            wallet,
            "{\"did\":\"%s\",\"verkey\":\"%s\"}".format(theirDidInfo.did, theirDidInfo.verkey)
        ).get()

        val metadata = Gson().toJson(
            PairwiseData(
                label,
                theirEndpoint,
                theirDidInfo.verkey,
                myDidInfo.verkey,
                theirRoutingKeys,
                userDeleted = false
            )
        ).replace("""\u003d""", "=")

        println(metadata)

        Pairwise.createPairwise(wallet, theirDidInfo.did, myDidInfo.did, metadata).get()
    }

    override fun savePendingState(outState: Bundle) {
    }

    // endregion

    // region interactor inputs


    // endregion
}
