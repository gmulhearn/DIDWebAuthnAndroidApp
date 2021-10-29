package com.gmulhearn.didwebauthn.core.protocols

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import co.nstant.`in`.cbor.CborBuilder
import co.nstant.`in`.cbor.CborEncoder
import co.nstant.`in`.cbor.CborException
import com.gmulhearn.didwebauthn.common.WalletProvider
import com.gmulhearn.didwebauthn.data.AllowCredentialDescriptor
import com.gmulhearn.didwebauthn.data.PublicKeyCredentialAssertionResponse
import com.gmulhearn.didwebauthn.data.PublicKeyCredentialAttestationResponse
import com.gmulhearn.didwebauthn.data.PublicKeyCredentialRpEntity
import com.gmulhearn.didwebauthn.data.PublicKeyCredentialUserEntity
import com.gmulhearn.didwebauthn.data.indy.DIDMetaData
import com.gmulhearn.didwebauthn.data.indy.EDDSA_ALG
import com.gmulhearn.didwebauthn.data.indy.ES256_ALG
import com.gmulhearn.didwebauthn.data.indy.KEY_ALG
import com.gmulhearn.didwebauthn.data.indy.LibIndyDIDListItem
import com.gmulhearn.didwebauthn.data.indy.WebAuthnDIDData
import com.gmulhearn.didwebauthn.data.indy.toSupportedKeyAlg
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.bitcoinj.core.Base58
import org.bitcoinj.core.Sha256Hash
import org.bouncycastle.crypto.prng.FixedSecureRandom
import org.hyperledger.indy.sdk.crypto.Crypto
import org.hyperledger.indy.sdk.did.Did
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECPoint
import javax.inject.Inject

class DIDAuthenticator @Inject constructor(
    val context: Context,
    val walletProvider: WalletProvider
) {

    companion object {
        const val KEY_ID_PREFIX = "did:webauthn:"
    }
    /************************************** REGISTRATION **************************/
    /**
     * Primary Authenticator function - makeCredential.
     * Creates an attestation object (none attestation) and formats
     * https://fidoalliance.org/specs/fido-v2.0-ps-20190130/fido-client-to-authenticator-protocol-v2.0-ps-20190130.html#authenticatorMakeCredential
     */
    fun authenticatorMakeCredential(
        clientDataHash: ByteArray? = null, // Not used as no Attestation produced
        rp: PublicKeyCredentialRpEntity,
        user: PublicKeyCredentialUserEntity,
        pubKeyCredParams: List<Pair<String, Long>>,
        clientDataJson: String
    ): PublicKeyCredentialAttestationResponse {

        if (pubKeyCredParams.none { (it.second.toInt() == EDDSA_ALG || it.second.toInt() == ES256_ALG) }) {
            // TODO - throw, unsupported
        }

        // TODO: more validity checks here

        val (authData, credentialId) = createAuthData(rp, user, pubKeyCredParams)

        val byteStream = ByteArrayOutputStream()
        CborEncoder(byteStream).encode(
            CborBuilder()
                .addMap()
                .put("authData", authData)
                .put("fmt", "none")         // none attestation
                .putMap("attStmt")      // empty map
                .end()
                .end()
                .build()
        )

        val attestObj = byteStream.toByteArray()

        // NOTE: difference from CTAP spec - the complete PublicKeyCredential response is constructed
        //  inside the authenticator for convenience.
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
    private fun createAuthData(
        rp: PublicKeyCredentialRpEntity,
        user: PublicKeyCredentialUserEntity,
        pubKeyCredParams: List<Pair<String, Long>>
    ): Pair<ByteArray, ByteArray> {
        // assume must be either eddsa or es256, (constraint verified by function caller)
        val keyAlg = // ES256_ALG
            if (pubKeyCredParams.any { it.second.toInt() == EDDSA_ALG }) EDDSA_ALG else ES256_ALG

        val rpIdHash = Sha256Hash.hash(rp.id.toByteArray(Charsets.UTF_8))
        val flags: Byte =
            0x01 or (0x01 shl 6) or (0x01 shl 2)// attested cred included and user verified
        val counter = 1

        /** create AttestedCredentialData */
        val didCred = createDidCredential(rp, user, keyAlg)
        val coseEncoded: ByteArray = when (didCred.keyAlg) {
            KEY_ALG.EDDSA -> {
                val edDSAPublicKey = Base58.decode(didCred.edDSAKey)
                coseEncodeEdDSAPublicKey(edDSAPublicKey)
            }
            KEY_ALG.ES256 -> {
                val es256PublicKey = generateES256KeyFromDID(didCred.edDSAKey).public
                coseEncodeES256PublicKey(es256PublicKey)
            }
        }

        val credentialId = didCred.keyId.toByteArray(Charsets.UTF_8)
        val aaguid = ByteArray(16).map { 0.toByte() }.toByteArray() // byte array of zeros

        val attestedCredDataBuff =
            ByteBuffer.allocate(16 + 2 + credentialId.size + coseEncoded.size)
        attestedCredDataBuff.put(aaguid)
        attestedCredDataBuff.putShort(credentialId.size.toShort())
        attestedCredDataBuff.put(credentialId)
        attestedCredDataBuff.put(coseEncoded)
        val attestedCredData = attestedCredDataBuff.array()

        /** **************************** */

        val authDataBuff = ByteBuffer.allocate(32 + 1 + 4 + attestedCredData.size)
        authDataBuff.put(rpIdHash)
        authDataBuff.put(flags)
        authDataBuff.putInt(counter)
        authDataBuff.put(attestedCredData)

        return Pair(authDataBuff.array(), credentialId)
    }

    /**
     * Encodes a EdDSAPublicKey into COSEKEY formatting
     */
    private fun coseEncodeEdDSAPublicKey(edDSAPubKey: ByteArray): ByteArray {
        val byteStream = ByteArrayOutputStream()
        try {
            CborEncoder(byteStream).encode(
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
            throw Exception("coseEncodePublicKey threw exception: ", e)
        }
        return byteStream.toByteArray()
    }

    /**
     * TODO - redo
     */
    fun coseEncodeES256PublicKey(publicKey: PublicKey): ByteArray {
        val ecPublicKey: ECPublicKey = publicKey as ECPublicKey
        val point: ECPoint = ecPublicKey.w

        val xVariableLength: ByteArray = point.affineX.toByteArray()
        val yVariableLength: ByteArray = point.affineY.toByteArray()
        val x: ByteArray = toUnsignedFixedLength(xVariableLength, 32)
        val y: ByteArray = toUnsignedFixedLength(yVariableLength, 32)
        val byteStream = ByteArrayOutputStream()
        try {
            CborEncoder(byteStream).encode(
                CborBuilder()
                    .addMap()
                    .put(1, 2) // kty - EC2 key type
                    .put(3, -7) // alg - ES256 sig algorithm
                    .put(-1, 1) // crv - P-256 curve
                    .put(-2, x) // x
                    .put(-3, y) // y
                    .end()
                    .build()
            )
        } catch (e: CborException) {

        }
        return byteStream.toByteArray()
    }

    /**
     * TODO - remove
     */
    private fun toUnsignedFixedLength(arr: ByteArray, fixedLength: Int): ByteArray {
        val fixed = ByteArray(fixedLength)
        val offset = fixedLength - arr.size
        val srcPos = (-offset).coerceAtLeast(0)
        val dstPos = offset.coerceAtLeast(0)
        val copyLength = arr.size.coerceAtMost(fixedLength)
        System.arraycopy(arr, srcPos, fixed, dstPos, copyLength)
        return fixed
    }
    /***********************************************************************************/


    /************************************** AUTHENTICATION ******************************/
    /**
     * Primary Authenticator function - getAssertion.
     * Creates an assertion object and formats
     * https://fidoalliance.org/specs/fido-v2.0-ps-20190130/fido-client-to-authenticator-protocol-v2.0-ps-20190130.html#authenticatorGetAssertion
     */
    fun authenticatorGetAssertion(
        rpId: String,
        clientDataHash: ByteArray,
        allowList: List<AllowCredentialDescriptor>,
        clientDataJson: String
    ): PublicKeyCredentialAssertionResponse {

        val didCred = getDidCredential(rpId, allowList)
        incrementCounterForDIDCred(didCred)
        val counter = didCred.authCounter + 1
        val authData = createAuthData(counter, didCred.rpInfo.id)

        val authDataClientDataHashToSign = mutableListOf<Byte>()
        authData.forEach { authDataClientDataHashToSign.add(it) }
        clientDataHash.forEach { authDataClientDataHashToSign.add(it) }

        val sig = when (didCred.keyAlg) {
            KEY_ALG.EDDSA -> {
                val keyToSignWith = didCred.edDSAKey
                Crypto.cryptoSign(
                    walletProvider.getWallet(),
                    keyToSignWith,
                    authDataClientDataHashToSign.toByteArray()
                ).get()
            }
            KEY_ALG.ES256 -> {
                val keyToSignWith =
                    getES256KeyFromDID(didVerkey = didCred.edDSAKey)    // should generate instead
                val signature = Signature.getInstance("SHA256withECDSA")
                signature.initSign(keyToSignWith.private)
                signature.update(authDataClientDataHashToSign.toByteArray())
                signature.sign()
            }
        }
        val id = didCred.keyId.toByteArray(Charsets.UTF_8)
        val user = didCred.userInfo.id

        // NOTE: difference from CTAP spec - the complete PublicKeyCredential response is constructed
        //  inside the authenticator for convenience.
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
        counter: Int,
        rpId: String
    ): ByteArray {

        val rpIdHash = Sha256Hash.hash(rpId.toByteArray(Charsets.UTF_8))
        val flags: Byte = 0x01 // no attest data include

        val authDataBuff = ByteBuffer.allocate(32 + 1 + 4)
        authDataBuff.put(rpIdHash)
        authDataBuff.put(flags)
        authDataBuff.putInt(counter)

        return authDataBuff.array()
    }
    /***********************************************************************************/


    /********************************* DID CREDENTIALS *********************************/

    private fun createDidCredential(
        rp: PublicKeyCredentialRpEntity,
        user: PublicKeyCredentialUserEntity,
        keyAlg: Int
    ): WebAuthnDIDData {
        val did = Did.createAndStoreMyDid(walletProvider.getWallet(), "{}").get()

        val webAuthnMetadata = WebAuthnDIDData(
            keyId = "$KEY_ID_PREFIX${did.did}",
            authCounter = 1,
            userInfo = user,
            rpInfo = PublicKeyCredentialRpEntity(rp.id, rp.name),
            edDSAKey = did.verkey,
            did = did.did,
            keyAlg = keyAlg.toSupportedKeyAlg()
        )

        val didMetadata = DIDMetaData(webAuthnData = webAuthnMetadata)

        Did.setDidMetadata(walletProvider.getWallet(), did.did, Gson().toJson(didMetadata)).get()

        return webAuthnMetadata
    }

    /**
     * get MetadataDID object that matches supplied opts
     */
    private fun getDidCredential(rpId: String, allowList: List<AllowCredentialDescriptor>): WebAuthnDIDData {
        val possibleWebAuthnDIDs = getWebAuthnDIDsData()

        // TODO - handle error if none found?
        return possibleWebAuthnDIDs.first { webAuthnDIDData ->
            // opts.rpId should be the full domain, and the rpId can be a substring
            val rpMatch = rpId == webAuthnDIDData.rpInfo.id
            println("DEBUG: allowed cred: ${rpId}")
            println("DEBUG: this cred: ${webAuthnDIDData.rpInfo.id}")
            println("DEBUG: RPMATCH: ${rpMatch}")

            val keyIdMatch = allowList.any {
                it.getId().contentEquals(webAuthnDIDData.keyId.toByteArray(Charsets.UTF_8))
            }

            rpMatch && keyIdMatch
        }
    }

    /**
     * increments the sign counter for a did
     */
    private fun incrementCounterForDIDCred(didCred: WebAuthnDIDData) {
        val newMetadata =
            DIDMetaData(webAuthnData = didCred.copy(authCounter = didCred.authCounter + 1))
        Did.setDidMetadata(walletProvider.getWallet(), didCred.did, Gson().toJson(newMetadata))
            .get()
    }

    /**
     * get DIDs stored in the DID wallet that have valid webAuthn credential metadata
     */
    private fun getWebAuthnDIDsData(): List<WebAuthnDIDData> {
        val myDids = Did.getListMyDidsWithMeta(walletProvider.getWallet()).get()
        val metadataDIDType = object : TypeToken<List<LibIndyDIDListItem>>() {}.type
        val didList = Gson().fromJson<List<LibIndyDIDListItem>>(myDids, metadataDIDType)

        val webauthnDIDDataList = mutableListOf<WebAuthnDIDData>()
        didList.forEach { didItem ->
            didItem.metadata?.let {
                try {
                    val metadata = Gson().fromJson(it, DIDMetaData::class.java)
                    metadata.webAuthnData?.let { webauthnDID ->
                        webauthnDIDDataList.add(webauthnDID)
                    }
                } catch (e: Exception) {
                }
            }
        }

        return webauthnDIDDataList
    }

    /***********************************************************************************/

    /********************************* KEY TRANSLATION *********************************/
    /**
     * TODO
     */
    private fun generateES256KeyFromDID(didVerkey: String): KeyPair {
        val seed = Crypto.cryptoSign(
            walletProvider.getWallet(),
            didVerkey,
            "seed".toByteArray(Charsets.UTF_8)  // TODO - is this completely secure?
        ).get()

        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            "ES256_FROM_DID_VERKEY_$didVerkey",
            KeyProperties.PURPOSE_SIGN
        )
            .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
            .setDigests((KeyProperties.DIGEST_SHA256))
            .build()

        val generator =
            KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore")
        generator.initialize(keyGenParameterSpec, FixedSecureRandom(seed))

        return generator.genKeyPair()
    }

    /**
     * TODO
     */
    private fun getES256KeyFromDID(didVerkey: String): KeyPair {
        val store = KeyStore.getInstance("AndroidKeyStore")
        store.load(null)

        val keyName = "ES256_FROM_DID_VERKEY_$didVerkey"

        return KeyPair(
            store.getCertificate(keyName).publicKey,
            store.getKey(keyName, null) as PrivateKey
        )
    }

    /***********************************************************************************/
}