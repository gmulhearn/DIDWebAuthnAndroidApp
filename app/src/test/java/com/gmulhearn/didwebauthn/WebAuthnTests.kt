package com.gmulhearn.didwebauthn

import com.gmulhearn.didwebauthn.data.PublicKeyCredentialCreationOptions
import com.gmulhearn.didwebauthn.core.protocols.getChallenge
import com.gmulhearn.didwebauthn.core.protocols.toAuthenticatorMakeCredentialOptions
import com.gmulhearn.didwebauthn.core.protocols.toDuoLabsAuthn
import com.google.gson.Gson
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class WebAuthnTests {
    @Before
    fun setup() {
    }

    @Test
    fun testJSONtoCredCreationObject() {
        val stringJSON = """
            {"publicKey":{"challenge":{"0":246,"1":148,"2":149,"3":59,"4":141,"5":103,"6":252,"7":187,"8":233,"9":143,"10":194,"11":55,"12":237,"13":148,"14":23,"15":52,"16":96,"17":57,"18":129,"19":189,"20":140,"21":101,"22":55,"23":54,"24":106,"25":105,"26":205,"27":159,"28":37,"29":138,"30":252,"31":227},"rp":{"name":"webauthn.io","id":"webauthn.io"},"user":{"name":"abcde","displayName":"abcde","id":{"0":255,"1":171,"2":12,"3":0,"4":0,"5":0,"6":0,"7":0,"8":0,"9":0}},"pubKeyCredParams":[{"type":"public-key","alg":-7},{"type":"public-key","alg":-35},{"type":"public-key","alg":-36},{"type":"public-key","alg":-257},{"type":"public-key","alg":-258},{"type":"public-key","alg":-259},{"type":"public-key","alg":-37},{"type":"public-key","alg":-38},{"type":"public-key","alg":-39},{"type":"public-key","alg":-8}],"authenticatorSelection":{"requireResidentKey":false,"userVerification":"discouraged"},"timeout":60000,"extensions":{"txAuthSimple":""},"attestation":"none"}}
        """.trimIndent()
        println(stringJSON)
        val actualCredentialCreationOptions =
            Gson().fromJson(stringJSON, PublicKeyCredentialCreationOptions::class.java)
        println(actualCredentialCreationOptions)
        Assert.assertEquals(
            actualCredentialCreationOptions.publicKey.relyingPartyInfo.id,
            "webauthn.io"
        )
        val actualChallengeBytes = actualCredentialCreationOptions.publicKey.getChallenge()
        val expectedChallengeBytes = arrayOf(
            246,
            148,
            149,
            59,
            141,
            103,
            252,
            187,
            233,
            143,
            194,
            55,
            237,
            148,
            23,
            52,
            96,
            57,
            129,
            189,
            140,
            101,
            55,
            54,
            106,
            105,
            205,
            159,
            37,
            138,
            252,
            227
        ).map { it.toByte() }
        actualChallengeBytes.zip(expectedChallengeBytes).forEach {
            Assert.assertEquals(it.second, it.first)
        }
    }

    @Test
    fun testCredCreationOptionToMakeCredential() {
        val stringJSON = """
            {"publicKey":{"challenge":{"0":246,"1":148,"2":149,"3":59,"4":141,"5":103,"6":252,"7":187,"8":233,"9":143,"10":194,"11":55,"12":237,"13":148,"14":23,"15":52,"16":96,"17":57,"18":129,"19":189,"20":140,"21":101,"22":55,"23":54,"24":106,"25":105,"26":205,"27":159,"28":37,"29":138,"30":252,"31":227},"rp":{"name":"webauthn.io","id":"webauthn.io"},"user":{"name":"abcde","displayName":"abcde","id":{"0":255,"1":171,"2":12,"3":0,"4":0,"5":0,"6":0,"7":0,"8":0,"9":0}},"pubKeyCredParams":[{"type":"public-key","alg":-7},{"type":"public-key","alg":-35},{"type":"public-key","alg":-36},{"type":"public-key","alg":-257},{"type":"public-key","alg":-258},{"type":"public-key","alg":-259},{"type":"public-key","alg":-37},{"type":"public-key","alg":-38},{"type":"public-key","alg":-39},{"type":"public-key","alg":-8}],"authenticatorSelection":{"requireResidentKey":false,"userVerification":"discouraged"},"timeout":60000,"extensions":{"txAuthSimple":""},"attestation":"none"}}
        """.trimIndent()
        val actualCredentialCreationOptions =
            Gson().fromJson(stringJSON, PublicKeyCredentialCreationOptions::class.java)
        val makecred =
            actualCredentialCreationOptions.publicKey.toAuthenticatorMakeCredentialOptions("https://webauthn.io/")
        println(makecred)
        val actualHexBytesOfHash =
            makecred.clientDataHash.toUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }
        Assert.assertEquals(
            "f6f2258019884b27097056983bf3dc9dae6a231ac4941bf2eba52056becd1343",
            actualHexBytesOfHash
        )
    }

    @Test
    fun testCredCreateOptsToDuoLabs() {
        val stringJSON = """
            {"publicKey":{"challenge":{"0":246,"1":148,"2":149,"3":59,"4":141,"5":103,"6":252,"7":187,"8":233,"9":143,"10":194,"11":55,"12":237,"13":148,"14":23,"15":52,"16":96,"17":57,"18":129,"19":189,"20":140,"21":101,"22":55,"23":54,"24":106,"25":105,"26":205,"27":159,"28":37,"29":138,"30":252,"31":227},"rp":{"name":"webauthn.io","id":"webauthn.io"},"user":{"name":"abcde","displayName":"abcde","id":{"0":255,"1":171,"2":12,"3":0,"4":0,"5":0,"6":0,"7":0,"8":0,"9":0}},"pubKeyCredParams":[{"type":"public-key","alg":-7},{"type":"public-key","alg":-35},{"type":"public-key","alg":-36},{"type":"public-key","alg":-257},{"type":"public-key","alg":-258},{"type":"public-key","alg":-259},{"type":"public-key","alg":-37},{"type":"public-key","alg":-38},{"type":"public-key","alg":-39},{"type":"public-key","alg":-8}],"authenticatorSelection":{"requireResidentKey":false,"userVerification":"discouraged"},"timeout":60000,"extensions":{"txAuthSimple":""},"attestation":"none"}}
        """.trimIndent()
        val actualCredentialCreationOptions =
            Gson().fromJson(stringJSON, PublicKeyCredentialCreationOptions::class.java)
        val makecred =
            actualCredentialCreationOptions.publicKey.toAuthenticatorMakeCredentialOptions("https://webauthn.io/")
        val duoLabs = makecred.toDuoLabsAuthn()
        println(duoLabs)
        println(duoLabs.areWellFormed())

    }
}