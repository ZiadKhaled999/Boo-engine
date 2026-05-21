package io.booengine.bridge

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BridgeResponseTest {

    // region success() factory

    @Test
    fun `success factory creates response with ok true`() {
        val response = BridgeResponse.success("req_001", JSONObject())

        assertTrue(response.ok)
    }

    @Test
    fun `success factory stores requestId`() {
        val response = BridgeResponse.success("req_001", JSONObject())

        assertEquals("req_001", response.requestId)
    }

    @Test
    fun `success factory stores data`() {
        val data = JSONObject().put("ack", true)
        val response = BridgeResponse.success("req_001", data)

        assertNotNull(response.data)
        assertTrue(response.data!!.getBoolean("ack"))
    }

    @Test
    fun `success factory sets error to null`() {
        val response = BridgeResponse.success("req_001", JSONObject())

        assertNull(response.error)
    }

    // endregion

    // region failure() factory

    @Test
    fun `failure factory creates response with ok false`() {
        val response = BridgeResponse.failure("req_001", "SOME_ERROR", "Some error message")

        assertFalse(response.ok)
    }

    @Test
    fun `failure factory stores requestId`() {
        val response = BridgeResponse.failure("req_002", "SOME_ERROR", "msg")

        assertEquals("req_002", response.requestId)
    }

    @Test
    fun `failure factory stores error code and message`() {
        val response = BridgeResponse.failure("req_001", "VALIDATION_ERROR", "Invalid payload")

        assertNotNull(response.error)
        assertEquals("VALIDATION_ERROR", response.error!!.code)
        assertEquals("Invalid payload", response.error!!.message)
    }

    @Test
    fun `failure factory stores optional details`() {
        val details = JSONObject().put("field", "namespace")
        val response = BridgeResponse.failure("req_001", "VALIDATION_ERROR", "msg", details)

        assertNotNull(response.error!!.details)
        assertEquals("namespace", response.error!!.details!!.getString("field"))
    }

    @Test
    fun `failure factory defaults details to null when not provided`() {
        val response = BridgeResponse.failure("req_001", "SOME_ERROR", "msg")

        assertNull(response.error!!.details)
    }

    @Test
    fun `failure factory sets data to null`() {
        val response = BridgeResponse.failure("req_001", "SOME_ERROR", "msg")

        assertNull(response.data)
    }

    // endregion

    // region toJsonString() — success path

    @Test
    fun `toJsonString for success response contains ok true`() {
        val json = JSONObject(BridgeResponse.success("req_001", JSONObject()).toJsonString())

        assertTrue(json.getBoolean("ok"))
    }

    @Test
    fun `toJsonString for success response contains requestId`() {
        val json = JSONObject(BridgeResponse.success("req_abc", JSONObject()).toJsonString())

        assertEquals("req_abc", json.getString("requestId"))
    }

    @Test
    fun `toJsonString for success response contains data field`() {
        val data = JSONObject().put("ack", true)
        val json = JSONObject(BridgeResponse.success("req_001", data).toJsonString())

        assertTrue(json.has("data"))
        assertTrue(json.getJSONObject("data").getBoolean("ack"))
    }

    @Test
    fun `toJsonString for success with null data emits empty data object`() {
        val response = BridgeResponse(ok = true, requestId = "req_001", data = null)
        val json = JSONObject(response.toJsonString())

        assertTrue(json.has("data"))
        assertEquals(0, json.getJSONObject("data").length())
    }

    @Test
    fun `toJsonString for success response does not contain error field`() {
        val json = JSONObject(BridgeResponse.success("req_001", JSONObject()).toJsonString())

        assertFalse(json.has("error"))
    }

    // endregion

    // region toJsonString() — failure path

    @Test
    fun `toJsonString for failure response contains ok false`() {
        val json = JSONObject(
            BridgeResponse.failure("req_001", "ERR", "msg").toJsonString()
        )

        assertFalse(json.getBoolean("ok"))
    }

    @Test
    fun `toJsonString for failure response contains requestId`() {
        val json = JSONObject(
            BridgeResponse.failure("req_xyz", "ERR", "msg").toJsonString()
        )

        assertEquals("req_xyz", json.getString("requestId"))
    }

    @Test
    fun `toJsonString for failure response contains error code and message`() {
        val json = JSONObject(
            BridgeResponse.failure("req_001", "VALIDATION_ERROR", "Invalid payload").toJsonString()
        )

        val error = json.getJSONObject("error")
        assertEquals("VALIDATION_ERROR", error.getString("code"))
        assertEquals("Invalid payload", error.getString("message"))
    }

    @Test
    fun `toJsonString for failure response contains empty details when null`() {
        val json = JSONObject(
            BridgeResponse.failure("req_001", "ERR", "msg").toJsonString()
        )

        val error = json.getJSONObject("error")
        assertTrue(error.has("details"))
        assertEquals(0, error.getJSONObject("details").length())
    }

    @Test
    fun `toJsonString for failure response includes provided details`() {
        val details = JSONObject().put("hint", "check namespace")
        val json = JSONObject(
            BridgeResponse.failure("req_001", "ERR", "msg", details).toJsonString()
        )

        val error = json.getJSONObject("error")
        assertEquals("check namespace", error.getJSONObject("details").getString("hint"))
    }

    @Test
    fun `toJsonString for failure with null error uses INTERNAL_ERROR defaults`() {
        val response = BridgeResponse(ok = false, requestId = "req_001", error = null)
        val json = JSONObject(response.toJsonString())

        val error = json.getJSONObject("error")
        assertEquals("INTERNAL_ERROR", error.getString("code"))
        assertEquals("Unknown bridge error", error.getString("message"))
    }

    @Test
    fun `toJsonString for failure response does not contain data field`() {
        val json = JSONObject(
            BridgeResponse.failure("req_001", "ERR", "msg").toJsonString()
        )

        assertFalse(json.has("data"))
    }

    // endregion

    // region toJsonString() — output is valid JSON

    @Test
    fun `toJsonString always returns parseable JSON string`() {
        val successJson = BridgeResponse.success("req_001", JSONObject().put("x", 1)).toJsonString()
        val failureJson = BridgeResponse.failure("req_002", "ERR", "msg").toJsonString()

        // Should not throw
        JSONObject(successJson)
        JSONObject(failureJson)
    }

    // endregion

    // region BridgeError data class

    @Test
    fun `BridgeError stores code and message`() {
        val error = BridgeError("MY_CODE", "my message")

        assertEquals("MY_CODE", error.code)
        assertEquals("my message", error.message)
    }

    @Test
    fun `BridgeError details defaults to null`() {
        val error = BridgeError("CODE", "msg")

        assertNull(error.details)
    }

    @Test
    fun `BridgeError equality holds for identical instances`() {
        val e1 = BridgeError("CODE", "msg")
        val e2 = BridgeError("CODE", "msg")

        assertEquals(e1, e2)
    }

    // endregion

    // region BridgeResponse data class

    @Test
    fun `BridgeResponse equality holds for identical success responses with same data reference`() {
        val data = JSONObject()
        val r1 = BridgeResponse(ok = true, requestId = "req_001", data = data)
        val r2 = BridgeResponse(ok = true, requestId = "req_001", data = data)

        assertEquals(r1, r2)
    }

    // endregion
}