package io.booengine.app

import org.json.JSONArray
import org.json.JSONObject

private const val DEFAULT_START_URL = "file:///android_asset/index.html"

data class BooConfig(
    val appId: String,
    val appName: String,
    val startUrl: String,
    val permissions: List<String>,
)

object BooConfigLoader {
    fun fromJsonString(rawJson: String): BooConfig {
        val json = JSONObject(rawJson)
        val appId = json.optString("appId", "io.booengine.app")
        val appName = json.optString("appName", "Boo Engine")
        val startUrl = json.optString("startUrl", DEFAULT_START_URL)
        val permissions = parsePermissions(json.optJSONArray("permissions"))

        return BooConfig(
            appId = appId,
            appName = appName,
            startUrl = startUrl.ifBlank { DEFAULT_START_URL },
            permissions = permissions,
        )
    }

    private fun parsePermissions(permissions: JSONArray?): List<String> {
        if (permissions == null) return emptyList()

        return buildList {
            for (index in 0 until permissions.length()) {
                val value = permissions.optString(index)
                if (value.isNotBlank()) add(value)
            }
        }
    }
}
