package io.booengine.bridge

import org.json.JSONException
import org.json.JSONObject

data class BridgeRequest(
    val namespace: String,
    val method: String,
    val payload: JSONObject,
    val requestId: String
) {
    val methodKey: String
        get() = "$namespace.$method"

    companion object {
        const val MAX_REQUEST_SIZE_BYTES = 32 * 1024

        fun fromJson(raw: String): BridgeRequest {
            if (raw.toByteArray(Charsets.UTF_8).size > MAX_REQUEST_SIZE_BYTES) {
                throw BridgePayloadTooLargeException("request exceeds max payload size")
            }

            val json = try {
                JSONObject(raw)
            } catch (_: JSONException) {
                throw BridgeValidationException("Request body must be valid JSON")
            }

            val namespace = json.optString("namespace", "").trim()
            val method = json.optString("method", "").trim()
            if (namespace.isEmpty() || method.isEmpty()) {
                throw BridgeValidationException("namespace and method are required")
            }

            val requestId = json.optString("requestId", "").trim()
            val payload = json.optJSONObject("payload") ?: JSONObject()

            return BridgeRequest(namespace = namespace, method = method, payload = payload, requestId = requestId)
        }
    }
}

class BridgeValidationException(message: String) : IllegalArgumentException(message)
class BridgePayloadTooLargeException(message: String) : IllegalArgumentException(message)
