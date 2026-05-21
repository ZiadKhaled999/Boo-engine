package io.booengine.bridge

import android.util.Log
import android.webkit.JavascriptInterface
import org.json.JSONObject

class BridgeManager(
    private val runtimeReadyProvider: () -> Boolean = { true },
    private val isMethodAllowed: (String) -> Boolean = { method -> method == "system.ping" }
) {
    private val seenRequestIds = LinkedHashSet<String>()

    @JavascriptInterface
    fun call(requestJson: String): String {
        val start = System.currentTimeMillis()
        var requestId = ""
        var namespace = ""
        var method = ""

        val response = try {
            val request = BridgeRequest.fromJson(requestJson)
            requestId = request.requestId
            namespace = request.namespace
            method = request.method

            if (!runtimeReadyProvider()) {
                BridgeResponse.failure(requestId, "RUNTIME_NOT_READY", "Runtime is not ready")
            } else if (requestId.isNotEmpty() && !seenRequestIds.add(requestId)) {
                BridgeResponse.failure(requestId, "DUPLICATE_REQUEST", "Request id has already been processed")
            } else if (!isMethodAllowed(request.methodKey)) {
                BridgeResponse.failure(requestId, "PERMISSION_DENIED", "Method is not enabled by runtime config")
            } else {
                dispatch(request)
            }
        } catch (_: BridgePayloadTooLargeException) {
            BridgeResponse.failure(requestId, "PAYLOAD_TOO_LARGE", "Bridge payload exceeds max size")
        } catch (_: BridgeValidationException) {
            BridgeResponse.failure(requestId, "VALIDATION_ERROR", "Invalid bridge payload")
        } catch (_: Exception) {
            BridgeResponse.failure(requestId, "INTERNAL_ERROR", "Unexpected bridge error")
        }

        val duration = System.currentTimeMillis() - start
        logOutcome(requestJson, requestId, namespace, method, duration, response)
        return response.toJsonString()
    }

    private fun dispatch(request: BridgeRequest): BridgeResponse {
        return when (request.methodKey) {
            "system.ping" -> BridgeResponse.success(
                requestId = request.requestId,
                data = JSONObject().put("ack", true)
            )

            else -> BridgeResponse.failure(
                requestId = request.requestId,
                code = "METHOD_NOT_FOUND",
                message = "Unsupported bridge method: ${request.methodKey}"
            )
        }
    }

    private fun logOutcome(
        rawRequest: String,
        requestId: String,
        namespace: String,
        method: String,
        durationMs: Long,
        response: BridgeResponse
    ) {
        val parsed = JSONObject(response.toJsonString())
        val outcome = if (parsed.optBoolean("ok")) "ok" else parsed.optJSONObject("error")?.optString("code", "error")
        val safeNamespace = namespace.ifEmpty { safeField(rawRequest, "namespace") }
        val safeMethod = method.ifEmpty { safeField(rawRequest, "method") }
        Log.i(TAG, "requestId=$requestId namespace=$safeNamespace method=$safeMethod duration=${durationMs}ms outcome=$outcome")
    }

    private fun safeField(rawJson: String, fieldName: String): String =
        try {
            JSONObject(rawJson).optString(fieldName, "")
        } catch (_: Exception) {
            ""
        }

    companion object {
        private const val TAG = "BooBridge"
    }
}
