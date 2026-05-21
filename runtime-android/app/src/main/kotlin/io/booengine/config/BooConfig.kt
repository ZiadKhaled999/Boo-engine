package io.booengine.config

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class BooConfig(
    val appId: String,
    val appName: String,
    val startUrl: String,
    val permissions: Set<String>,
    val plugins: Map<String, Boolean>,
) {
    fun isPluginEnabled(namespace: String): Boolean = plugins[namespace] ?: false

    fun hasPermission(permission: String): Boolean = permissions.contains(permission)

    fun asMetadataJson(): JSONObject = JSONObject()
        .put("appId", appId)
        .put("appName", appName)
        .put("startUrl", startUrl)

    fun asPermissionsJson(): JSONArray = JSONArray(permissions.sorted())

    companion object {
        private const val DEFAULT_ASSET_PATH = "boo.config.json"

        fun fromAssets(context: Context, assetPath: String = DEFAULT_ASSET_PATH): BooConfig {
            return runCatching {
                context.assets.open(assetPath).use { stream ->
                    fromJson(JSONObject(stream.bufferedReader().readText()))
                }
            }.getOrElse { default() }
        }

        fun fromJson(body: JSONObject): BooConfig {
            val permissions = mutableSetOf<String>()
            val permissionsJson = body.optJSONArray("permissions") ?: JSONArray()
            for (i in 0 until permissionsJson.length()) {
                permissions.add(permissionsJson.optString(i))
            }

            val plugins = mutableMapOf<String, Boolean>()
            val pluginsJson = body.optJSONObject("plugins") ?: JSONObject()
            pluginsJson.keys().forEach { key ->
                plugins[key] = pluginsJson.optBoolean(key, false)
            }

            return BooConfig(
                appId = body.optString("appId", "io.booengine.app"),
                appName = body.optString("appName", "Boo App"),
                startUrl = body.optString("startUrl", "file:///android_asset/index.html"),
                permissions = permissions,
                plugins = plugins,
            )
        }

        fun default() = BooConfig(
            appId = "io.booengine.app",
            appName = "Boo App",
            startUrl = "file:///android_asset/index.html",
            permissions = emptySet(),
            plugins = mapOf("app" to true),
        )
    }
}
