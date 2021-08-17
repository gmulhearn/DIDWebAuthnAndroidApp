package com.gmulhearn.didwebauthn.core.protocols

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import co.nstant.`in`.cbor.CborBuilder
import co.nstant.`in`.cbor.CborEncoder
import co.nstant.`in`.cbor.CborException
import com.gmulhearn.didwebauthn.common.WalletProvider
import com.gmulhearn.didwebauthn.data.AuthenticatorGetAssertionOptions
import com.gmulhearn.didwebauthn.data.AuthenticatorMakeCredentialOptions
import com.gmulhearn.didwebauthn.data.PublicKeyCredentialAssertionResponse
import com.gmulhearn.didwebauthn.data.PublicKeyCredentialAttestationResponse
import com.gmulhearn.didwebauthn.data.RelyingPartyInfo
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
import java.util.Arrays
import javax.inject.Inject

class DIDAuthenticator @Inject constructor(
    val context: Context,
    val walletProvider: WalletProvider
) {
    /************************************** REGISTRATION **************************/
    /**
     * Primary Authenticator function - makeCredential.
     * Creates an attestation object (none attestation) and formats
     */
    fun makeCredentials(
        credOpts: AuthenticatorMakeCredentialOptions,
        clientDataJson: String
    ): PublicKeyCredentialAttestationResponse {

        if (credOpts.pubKeyCredParams.none { (it.second.toInt() == EDDSA_ALG || it.second.toInt() == ES256_ALG) }) {
            // throw, unsupported
        }

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
        // assume must be either eddsa or es256, (constraint verified by function caller)
        val keyAlg = // ES256_ALG
            if (credOpts.pubKeyCredParams.any { it.second.toInt() == EDDSA_ALG }) EDDSA_ALG else ES256_ALG

        val rpIdHash = Sha256Hash.hash(credOpts.rp.id.toByteArray(Charsets.UTF_8))
        val flags: Byte = 0x01 or (0x01 shl 6) // attested cred included
        val counter = 0

        /** create AttestedCredentialData */
        val didCred = createDidCredential(credOpts, keyAlg)
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

        val id = didCred.keyId.toByteArray(Charsets.UTF_8)
        val aaguid = ByteArray(16).map { 0.toByte() }.toByteArray() // byte array of zeros

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
     */
    fun getAssertion(
        assertionOpts: AuthenticatorGetAssertionOptions,
        clientDataJson: String
    ): PublicKeyCredentialAssertionResponse {
        // val counter = 1 // TODO - make for real

        val didCred = getDidCredential(assertionOpts)
        val counter = didCred.authCounter  // TODO - increment
        val authData = createAuthData(assertionOpts, counter)

        val authDataClientDataHashToSign = mutableListOf<Byte>()
        authData.forEach { authDataClientDataHashToSign.add(it) }
        assertionOpts.clientDataHash.forEach { authDataClientDataHashToSign.add(it) }

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

    private fun createDidCredential(
        opts: AuthenticatorMakeCredentialOptions,
        keyAlg: Int
    ): WebAuthnDIDData {
        val did = Did.createAndStoreMyDid(walletProvider.getWallet(), "{}").get()

        val webAuthnMetadata = WebAuthnDIDData(
            keyId = did.did,
            authCounter = 0,
            userInfo = opts.user,
            rpInfo = RelyingPartyInfo(opts.rp.name, opts.rp.id),
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
    private fun getDidCredential(opts: AuthenticatorGetAssertionOptions): WebAuthnDIDData {
        val possibleWebAuthnDIDs = getWebAuthnDIDsData()

        // TODO - handle error if none found?
        return possibleWebAuthnDIDs.first { webAuthnDIDData ->
            val rpMatch = webAuthnDIDData.rpInfo.id == opts.rpId
            val keyIdMatch = opts.allowCredentialDescriptorList.any {
                it.getId().contentEquals(webAuthnDIDData.keyId.toByteArray(Charsets.UTF_8))
            }

            rpMatch && keyIdMatch
        }
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


    /** GRAVEYARD */
//    private fun createAndGetDidVerkey(): ByteArray {
//        val myDids = Did.getListMyDidsWithMeta(walletProvider.getWallet()).get()
//        val myDidsJSON = JSONObject("{ \"dids\": $myDids}")
//
//        val myDidVer = myDidsJSON.getJSONArray("dids")
//            .getJSONObject(0).getString("verkey")
//        return Base58.decode(myDidVer)
//    }
//
//    private fun getDidVerkey(): ByteArray {
//        val myDids = Did.getListMyDidsWithMeta(walletProvider.getWallet()).get()
//        val myDidsJSON = JSONObject("{ \"dids\": $myDids}")
//
//        val myDidVer = myDidsJSON.getJSONArray("dids")
//            .getJSONObject(0).getString("verkey")
//        return Base58.decode(myDidVer)
//    }

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