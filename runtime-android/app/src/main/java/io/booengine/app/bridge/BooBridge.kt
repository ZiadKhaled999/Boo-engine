package io.booengine.app.bridge

import android.webkit.JavascriptInterface

class BooBridge(
    private val bridgeManager: BridgeManager,
) {
    @JavascriptInterface
    fun call(rawRequest: String): String {
        return bridgeManager.handle(rawRequest)
    }
}
