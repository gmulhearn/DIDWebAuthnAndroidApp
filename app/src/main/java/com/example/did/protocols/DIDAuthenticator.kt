package com.example.did.protocols

import android.content.Context
import co.nstant.`in`.cbor.CborBuilder
import co.nstant.`in`.cbor.CborEncoder
import co.nstant.`in`.cbor.CborException
import com.example.did.common.WalletProvider
import com.example.did.data.*
import org.bitcoinj.core.Base58
import org.bitcoinj.core.Sha256Hash
import org.hyperledger.indy.sdk.crypto.Crypto
import org.hyperledger.indy.sdk.did.Did
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.*
import javax.inject.Inject

class DIDAuthenticator @Inject constructor(
    val context: Context,
    val walletProvider: WalletProvider
) {
    // TODO - REMOVE BELOW - temporary measure
    private var userEntity = PublicKeyCredentialUserEntity(byteArrayOf(1), "todo", "todo")

    /************************************** REGISTRATION **************************/
    /**
     * Primary Authenticator function - makeCredential.
     * Creates an attestation object (none attestation) and formats
     */
    fun makeCredentials(
        credOpts: AuthenticatorMakeCredentialOptions,
        clientDataJson: String
    ): PublicKeyCredentialAttestationResponse {

        // TODO -remove below - temp
        userEntity = credOpts.user

        // TODO: validity checks here

        val authData = createAuthData(credOpts)

        // TODO: should just return below rather than extract like this
        val l = (authData[53].toInt() shl 8) + authData[54]
        val credentialId = Arrays.copyOfRange(authData, 55, 55 + l)

        val baos = ByteArrayOutputStream()
        CborEncoder(baos).encode(
            CborBuilder()
                .addMap()
                .put("authData", authData)
                .put("fmt", "none")
                .putMap("attStmt")
                .end()
                .end()
                .build()
        )

        val attestObj = baos.toByteArray()

        return createPublicKeyCredentialAttestationResponse(
            rawId = credentialId,
            attestationObject = attestObj,
            clientDataJson = clientDataJson.toByteArray(Charsets.UTF_8)
        )
    }

    /**
     * creates the authData component (FOR REGISTRATION) on the attestation object
     * see: https://www.w3.org/TR/webauthn/#attestation-object
     */
    private fun createAuthData(credOpts: AuthenticatorMakeCredentialOptions): ByteArray {

        val rpIdHash = Sha256Hash.hash(credOpts.rp.id.toByteArray(Charsets.UTF_8))
        val flags: Byte = 0x01 or (0x01 shl 6) // attested cred included
        val counter = 0

        /** create AttestedCredentialData */
        val edDSAPublicKey = createAndGetDidVerkey()
        val coseEncoded = coseEncodeEdDSAPublicKey(edDSAPublicKey)

        val id = "PtbVIGUBHKiHoBPQPGR72tEwMubp4nzUFmHnuGabinM".toByteArray(Charsets.UTF_8) // TODO
        val aaguid = ByteArray(16).map { 0.toByte() }.toByteArray()

        val attestedCredDataBuff = ByteBuffer.allocate(16 + 2 + id.size + coseEncoded.size)
        attestedCredDataBuff.put(aaguid)
        attestedCredDataBuff.putShort(id.size.toShort())
        attestedCredDataBuff.put(id)
        attestedCredDataBuff.put(coseEncoded)
        val attestedCredData = attestedCredDataBuff.array()

        /** **************************** */

        val authDataBuff = ByteBuffer.allocate(32 + 1 + 4 + attestedCredData.size)
        authDataBuff.put(rpIdHash)
        authDataBuff.put(flags)
        authDataBuff.putInt(counter)
        authDataBuff.put(attestedCredData)

        return authDataBuff.array()
    }

    /**
     * Encodes a EdDSAPublicKey into COSEKEY formatting
     */
    private fun coseEncodeEdDSAPublicKey(edDSAPubKey: ByteArray): ByteArray {
        val baos = ByteArrayOutputStream()
        try {
            CborEncoder(baos).encode(
                CborBuilder()
                    .addMap()
                    .put(1, 1) // kty - OKP key type
                    .put(3, -8) // alg - EdDSA Algo
                    .put(-1, 6) // crv - Ed25519 Curve
                    .put(
                        -2,
                        edDSAPubKey
                    ) // "x"  (helpful: https://github.com/Yubico/libfido2/issues/136)
                    .end()
                    .build()
            )
        } catch (e: CborException) {
            throw Exception("couldn't serialize to cbor", e)
        }
        return baos.toByteArray()
    }
    /***********************************************************************************/


    /************************************** AUTHENTICATION ******************************/
    /**
     * Primary Authenticator function - getAssertion.
     * Creates an assertion object and formats
     */
    fun getAssertion(
        assertionOpts: AuthenticatorGetAssertionOptions,
        clientDataJson: String
    ): PublicKeyCredentialAssertionResponse {

        val edDSAPublicKey = getDidVerkey()

        val counter = 1 // TODO - make for real

        val authData = createAuthData(assertionOpts, counter)

        val toSign = mutableListOf<Byte>()
        authData.forEach { toSign.add(it) }
        assertionOpts.clientDataHash.forEach { toSign.add(it) }

        val sig = Crypto.cryptoSign(
            walletProvider.getWallet(),
            Base58.encode(edDSAPublicKey),
            toSign.toByteArray()
        ).get()

        val id = "PtbVIGUBHKiHoBPQPGR72tEwMubp4nzUFmHnuGabinM".toByteArray(Charsets.UTF_8)  // TODO
        val user = userEntity.id

        return createPublicKeyCredentialAssertionResponse(
            rawId = id,
            authenticatorData = authData,
            clientDataJson = clientDataJson.toByteArray(Charsets.UTF_8),
            signature = sig,
            userHandle = user
        )
    }

    /**
     * creates the authData component (FOR AUTHENTICATION) on the attestation object
     * see: https://www.w3.org/TR/webauthn/#attestation-object
     */
    private fun createAuthData(
        credOpts: AuthenticatorGetAssertionOptions,
        counter: Int
    ): ByteArray {

        val rpIdHash = Sha256Hash.hash(credOpts.rpId.toByteArray(Charsets.UTF_8))
        val flags: Byte = 0x01 // no attest data include

        val authDataBuff = ByteBuffer.allocate(32 + 1 + 4)
        authDataBuff.put(rpIdHash)
        authDataBuff.put(flags)
        authDataBuff.putInt(counter)

        return authDataBuff.array()
    }
    /***********************************************************************************/


    /********************************* DID CREDENTIALS *********************************/

    /**
     * TODO - make this work
     */
    private fun createAndGetDidVerkey(): ByteArray {
        val myDids = Did.getListMyDidsWithMeta(walletProvider.getWallet()).get()
        val myDidsJSON = JSONObject("{ \"dids\": $myDids}")

        val myDidVer = myDidsJSON.getJSONArray("dids")
            .getJSONObject(0).getString("verkey")
        return Base58.decode(myDidVer)
    }

    /**
     * TODO - make this work
     */
    private fun getDidVerkey(): ByteArray {
        val myDids = Did.getListMyDidsWithMeta(walletProvider.getWallet()).get()
        val myDidsJSON = JSONObject("{ \"dids\": $myDids}")

        val myDidVer = myDidsJSON.getJSONArray("dids")
            .getJSONObject(0).getString("verkey")
        return Base58.decode(myDidVer)
    }

    /***********************************************************************************/


    /** GRAVEYARD DUOLABS IMPL BELOW */
//    private fun duoLabsMakeCredentials(
//        credOpts: AuthenticatorMakeCredentialOptions,
//        clientDataJson: String
//    ): PublicKeyCredentialAttestationResponse {
//        val duoLabsMakeCreds = credOpts.toDuoLabsAuthn()
//        println(duoLabsMakeCreds.credTypesAndPubKeyAlgs)
//
//        val attestationObject = duoLabsAuthn.makeCredential(duoLabsMakeCreds)
//        println(attestationObject)
//        println(attestationObject.credentialId)
//        println(attestationObject.credentialIdBase64)
//        println(attestationObject.asCBOR())
//        return createPublicKeyCredentialAttestationResponse(
//            rawId = attestationObject.credentialId,
//            attestationObject = attestationObject.asCBOR(),
//            clientDataJson = clientDataJson.toByteArray(Charsets.UTF_8)
//        )
//    }
//
//    fun duoLabsGetAssertion(
//        assertionOpts: AuthenticatorGetAssertionOptions,
//        clientDataJson: String
//    ): PublicKeyCredentialAssertionResponse {
//        val duoLabsGetAssertionOpts = assertionOpts.toDuoLabAuthn()
//
//        val assertionResult =
//            duoLabsAuthn.getAssertion(duoLabsGetAssertionOpts, null)
//
//        return createPublicKeyCredentialAssertionResponse(
//            rawId = assertionResult.selectedCredentialId,
//            authenticatorData = assertionResult.authenticatorData,
//            clientDataJson = clientDataJson.toByteArray(Charsets.UTF_8),
//            signature = assertionResult.signature,
//            userHandle = assertionResult.selectedCredentialUserHandle
//        )
//    }
}