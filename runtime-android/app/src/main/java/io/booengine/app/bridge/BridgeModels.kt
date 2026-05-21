package io.booengine.app.bridge

import org.json.JSONArray
import org.json.JSONObject

data class BridgeRequest(
    val requestId: String,
    val plugin: String,
    val method: String,
    val payload: JSONObject,
)

data class BridgeError(
    val code: String,
    val message: String,
)

data class BridgeResponse(
    val requestId: String,
    val ok: Boolean,
    val result: JSONObject? = null,
    val error: BridgeError? = null,
) {
    fun toJsonString(): String {
        val json = JSONObject()
            .put("requestId", requestId)
            .put("ok", ok)

        if (result != null) {
            json.put("result", result)
        }

        if (error != null) {
            json.put(
                "error",
                JSONObject()
                    .put("code", error.code)
                    .put("message", error.message),
            )
        }

        return json.toString()
    }

    companion object {
        fun fromJsonString(jsonString: String): BridgeResponse {
            val json = JSONObject(jsonString)
            val errorObj = json.optJSONObject("error")
            val error = if (errorObj == null) {
                null
            } else {
                BridgeError(
                    code = errorObj.optString("code", "UNKNOWN"),
                    message = errorObj.optString("message", "Unknown error"),
                )
            }

            return BridgeResponse(
                requestId = json.optString("requestId", ""),
                ok = json.optBoolean("ok", false),
                result = json.optJSONObject("result"),
                error = error,
            )
        }

        fun invalidRequest(message: String): BridgeResponse {
            return BridgeResponse(
                requestId = "",
                ok = false,
                error = BridgeError("INVALID_REQUEST", message),
            )
        }
    }
}

object BridgeRequestParser {
    fun parse(raw: String): Result<BridgeRequest> {
        return runCatching {
            val json = JSONObject(raw)
            BridgeRequest(
                requestId = json.getString("requestId"),
                plugin = json.getString("plugin"),
                method = json.getString("method"),
                payload = json.optJSONObject("payload") ?: JSONObject(),
            )
        }
    }
}

object PermissionGate {
    fun hasPermission(allowedPermissions: List<String>, requiredPermission: String): Boolean {
        return allowedPermissions.contains(requiredPermission)
    }
}

object AppPlugin {
    const val PLUGIN_ID = "app"
    const val GET_METADATA = "getMetadata"
    const val REQUIRED_PERMISSION = "app:read:metadata"

    fun execute(method: String, payload: JSONObject, appId: String, appName: String): BridgeResponse {
        return when (method) {
            GET_METADATA -> {
                val includePermissions = payload.optBoolean("includePermissions", false)
                val result = JSONObject()
                    .put("appId", appId)
                    .put("appName", appName)

                if (includePermissions) {
                    result.put("permissions", JSONArray().put(REQUIRED_PERMISSION))
                }

                BridgeResponse(
                    requestId = "",
                    ok = true,
                    result = result,
                )
            }

            else -> BridgeResponse(
                requestId = "",
                ok = false,
                error = BridgeError("METHOD_NOT_FOUND", "Unknown method '$method' for plugin '$PLUGIN_ID'"),
            )
        }
    }
}

class BridgeManager(
    private val appId: String,
    private val appName: String,
    private val allowedPermissions: List<String>,
) {
    fun handle(rawRequest: String): String {
        val request = BridgeRequestParser.parse(rawRequest).getOrElse {
            return BridgeResponse.invalidRequest("Invalid JSON payload").toJsonString()
        }

        if (request.plugin != AppPlugin.PLUGIN_ID) {
            return BridgeResponse(
                requestId = request.requestId,
                ok = false,
                error = BridgeError("PLUGIN_NOT_FOUND", "Unknown plugin '${request.plugin}'"),
            ).toJsonString()
        }

        if (!PermissionGate.hasPermission(allowedPermissions, AppPlugin.REQUIRED_PERMISSION)) {
            return BridgeResponse(
                requestId = request.requestId,
                ok = false,
                error = BridgeError("PERMISSION_DENIED", "Missing permission '${AppPlugin.REQUIRED_PERMISSION}'"),
            ).toJsonString()
        }

        val pluginResponse = AppPlugin.execute(
            method = request.method,
            payload = request.payload,
            appId = appId,
            appName = appName,
        )

        return pluginResponse.copy(requestId = request.requestId).toJsonString()
    }
}
