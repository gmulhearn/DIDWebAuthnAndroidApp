package com.gmulhearn.didwebauthn

import com.gmulhearn.didwebauthn.protocols.CCDToString
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class UtilTests {


    @Before
    fun setup() {

    }

    @Test
    fun addition_isCorrect() {
        Assert.assertEquals(4, 2 + 2)
    }

    @Test
    fun testCCDToString() {
        val string1 = """a quote: " and a slash: \ """
        println(string1.CCDToString())
        Assert.assertEquals(""""a quote: \" and a slash: \\ """", string1.CCDToString())

        val string2 = """a slash \ and now some bytes:""" +
                byteArrayOf(1, 2, 3, 10, 14, 20, 8)
                    .toString(Charsets.UTF_8)
        val expected2 = """"a slash \\ and now some bytes:\u0001\u0002\u0003\u000a\u000e\u0014\u0008""""
        println(string2.CCDToString())
        Assert.assertEquals(expected2, string2.CCDToString())
    }
}