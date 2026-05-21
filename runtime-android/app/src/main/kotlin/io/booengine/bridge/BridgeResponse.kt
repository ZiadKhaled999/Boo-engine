package io.booengine.bridge

import org.json.JSONObject

data class BridgeError(
    val code: String,
    val message: String,
    val details: JSONObject? = null
)

data class BridgeResponse(
    val ok: Boolean,
    val requestId: String,
    val data: JSONObject? = null,
    val error: BridgeError? = null
) {
    fun toJsonString(): String {
        val json = JSONObject()
            .put("ok", ok)
            .put("requestId", requestId)

        if (ok) {
            json.put("data", data ?: JSONObject())
        } else {
            val err = error ?: BridgeError("INTERNAL_ERROR", "Unknown bridge error")
            json.put(
                "error",
                JSONObject()
                    .put("code", err.code)
                    .put("message", err.message)
                    .put("details", err.details ?: JSONObject())
            )
        }

        return json.toString()
    }

    companion object {
        fun success(requestId: String, data: JSONObject): BridgeResponse =
            BridgeResponse(ok = true, requestId = requestId, data = data)

        fun failure(requestId: String, code: String, message: String, details: JSONObject? = null): BridgeResponse =
            BridgeResponse(
                ok = false,
                requestId = requestId,
                error = BridgeError(code = code, message = message, details = details)
            )
    }
}
