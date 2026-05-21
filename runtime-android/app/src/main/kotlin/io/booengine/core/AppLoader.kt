package io.booengine.core

import android.net.Uri

class AppLoader(
    private val localAssetUrl: String = LOCAL_ASSET_URL,
) {
    fun resolveStartUrl(devUrl: String?): String {
        val sanitized = devUrl?.trim().orEmpty()
        if (sanitized.isEmpty()) return localAssetUrl

        val parsed = Uri.parse(sanitized)
        val scheme = parsed.scheme?.lowercase()
        return if (scheme == "http" || scheme == "https") {
            sanitized
        } else {
            localAssetUrl
        }
    }

    fun shouldOverrideUrlLoading(url: String?): Boolean {
        if (url.isNullOrBlank()) return true

        val parsed = Uri.parse(url)
        return parsed.scheme !in ALLOWED_SCHEMES
    }

    companion object {
        private const val LOCAL_ASSET_URL = "file:///android_asset/index.html"
        private val ALLOWED_SCHEMES = setOf("http", "https", "file", "about", "data")
    }
}
