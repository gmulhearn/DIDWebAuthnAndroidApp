package com.gmulhearn.didwebauthn.core.transport

import android.webkit.JavascriptInterface
import androidx.annotation.Keep

@Keep
class WebRTCJsInterface(
    private val onSignalled: (data: String) -> Unit,
    private val onDataReceived: (data: String) -> Unit
) {

    @JavascriptInterface
    fun signalled(data: String) {
        onSignalled(data)
    }

    @JavascriptInterface
    fun dataReceived(data: String) {
        onDataReceived(data)
    }
}