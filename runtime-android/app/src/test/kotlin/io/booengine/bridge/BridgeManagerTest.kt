package io.booengine.bridge

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for [BridgeManager].
 *
 * Robolectric is used so that android.util.Log calls inside BridgeManager become no-ops on the JVM.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class BridgeManagerTest {

    private fun validPingJson(requestId: String = "req_001"): String =
        """{"namespace":"system","method":"ping","requestId":"$requestId"}"""

    private fun parseResponse(raw: String): JSONObject = JSONObject(raw)

    // region system.ping — happy path

    @Test
    fun `call with valid system ping returns ok true`() {
        val manager = BridgeManager()
        val result = parseResponse(manager.call(validPingJson()))

        assertTrue(result.getBoolean("ok"))
    }

    @Test
    fun `call with valid system ping returns ack true in data`() {
        val manager = BridgeManager()
        val result = parseResponse(manager.call(validPingJson()))

        assertTrue(result.getJSONObject("data").getBoolean("ack"))
    }

    @Test
    fun `call with valid system ping echoes requestId`() {
        val manager = BridgeManager()
        val result = parseResponse(manager.call(validPingJson("req_xyz")))

        assertEquals("req_xyz", result.getString("requestId"))
    }

    @Test
    fun `call with no requestId still returns ok for system ping`() {
        val manager = BridgeManager()
        val json = """{"namespace":"system","method":"ping"}"""
        val result = parseResponse(manager.call(json))

        assertTrue(result.getBoolean("ok"))
        assertEquals("", result.getString("requestId"))
    }

    // endregion

    // region runtime not ready

    @Test
    fun `call returns RUNTIME_NOT_READY when runtimeReadyProvider returns false`() {
        val manager = BridgeManager(runtimeReadyProvider = { false })
        val result = parseResponse(manager.call(validPingJson()))

        assertFalse(result.getBoolean("ok"))
        assertEquals("RUNTIME_NOT_READY", result.getJSONObject("error").getString("code"))
    }

    @Test
    fun `call RUNTIME_NOT_READY echoes requestId`() {
        val manager = BridgeManager(runtimeReadyProvider = { false })
        val result = parseResponse(manager.call(validPingJson("req_rr1")))

        assertEquals("req_rr1", result.getString("requestId"))
    }

    // endregion

    // region duplicate request detection

    @Test
    fun `call returns DUPLICATE_REQUEST on second call with same requestId`() {
        val manager = BridgeManager()
        manager.call(validPingJson("req_dup"))
        val result = parseResponse(manager.call(validPingJson("req_dup")))

        assertFalse(result.getBoolean("ok"))
        assertEquals("DUPLICATE_REQUEST", result.getJSONObject("error").getString("code"))
    }

    @Test
    fun `call succeeds on second call with different requestId`() {
        val manager = BridgeManager()
        manager.call(validPingJson("req_a"))
        val result = parseResponse(manager.call(validPingJson("req_b")))

        assertTrue(result.getBoolean("ok"))
    }

    @Test
    fun `call does not deduplicate when requestId is empty`() {
        val manager = BridgeManager()
        val json = """{"namespace":"system","method":"ping"}"""
        manager.call(json)
        val result = parseResponse(manager.call(json))

        // Empty requestId should not trigger dedup — both calls should succeed
        assertTrue(result.getBoolean("ok"))
    }

    // endregion

    // region method permission enforcement

    @Test
    fun `call returns PERMISSION_DENIED when isMethodAllowed returns false`() {
        val manager = BridgeManager(isMethodAllowed = { false })
        val result = parseResponse(manager.call(validPingJson()))

        assertFalse(result.getBoolean("ok"))
        assertEquals("PERMISSION_DENIED", result.getJSONObject("error").getString("code"))
    }

    @Test
    fun `call PERMISSION_DENIED echoes requestId`() {
        val manager = BridgeManager(isMethodAllowed = { false })
        val result = parseResponse(manager.call(validPingJson("req_pd1")))

        assertEquals("req_pd1", result.getString("requestId"))
    }

    @Test
    fun `call succeeds for system ping when isMethodAllowed explicitly allows it`() {
        val manager = BridgeManager(isMethodAllowed = { method -> method == "system.ping" })
        val result = parseResponse(manager.call(validPingJson()))

        assertTrue(result.getBoolean("ok"))
    }

    // endregion

    // region unknown / unsupported method

    @Test
    fun `call returns METHOD_NOT_FOUND for allowed but unimplemented method`() {
        val manager = BridgeManager(isMethodAllowed = { true })
        val json = """{"namespace":"custom","method":"unknown","requestId":"req_001"}"""
        val result = parseResponse(manager.call(json))

        assertFalse(result.getBoolean("ok"))
        assertEquals("METHOD_NOT_FOUND", result.getJSONObject("error").getString("code"))
    }

    // endregion

    // region validation and payload size errors

    @Test
    fun `call returns VALIDATION_ERROR for invalid JSON input`() {
        val manager = BridgeManager()
        val result = parseResponse(manager.call("not valid json {{{"))

        assertFalse(result.getBoolean("ok"))
        assertEquals("VALIDATION_ERROR", result.getJSONObject("error").getString("code"))
    }

    @Test
    fun `call returns VALIDATION_ERROR when namespace is missing`() {
        val manager = BridgeManager()
        val result = parseResponse(manager.call("""{"method":"ping","requestId":"req_001"}"""))

        assertFalse(result.getBoolean("ok"))
        assertEquals("VALIDATION_ERROR", result.getJSONObject("error").getString("code"))
    }

    @Test
    fun `call returns VALIDATION_ERROR when method is missing`() {
        val manager = BridgeManager()
        val result = parseResponse(manager.call("""{"namespace":"system","requestId":"req_001"}"""))

        assertFalse(result.getBoolean("ok"))
        assertEquals("VALIDATION_ERROR", result.getJSONObject("error").getString("code"))
    }

    @Test
    fun `call returns PAYLOAD_TOO_LARGE when request exceeds 32KB`() {
        val manager = BridgeManager(isMethodAllowed = { true })
        val bigValue = "x".repeat(BridgeRequest.MAX_REQUEST_SIZE_BYTES + 1)
        val json = """{"namespace":"system","method":"ping","v":"$bigValue"}"""
        val result = parseResponse(manager.call(json))

        assertFalse(result.getBoolean("ok"))
        assertEquals("PAYLOAD_TOO_LARGE", result.getJSONObject("error").getString("code"))
    }

    @Test
    fun `call returns VALIDATION_ERROR for empty string input`() {
        val manager = BridgeManager()
        val result = parseResponse(manager.call(""))

        assertFalse(result.getBoolean("ok"))
        assertEquals("VALIDATION_ERROR", result.getJSONObject("error").getString("code"))
    }

    // endregion

    // region internal error handling

    @Test
    fun `call returns INTERNAL_ERROR when isMethodAllowed throws unexpectedly`() {
        val manager = BridgeManager(isMethodAllowed = { throw RuntimeException("unexpected") })
        val result = parseResponse(manager.call(validPingJson()))

        assertFalse(result.getBoolean("ok"))
        assertEquals("INTERNAL_ERROR", result.getJSONObject("error").getString("code"))
    }

    // endregion

    // region response structure

    @Test
    fun `call response is always valid JSON`() {
        val manager = BridgeManager()
        // Success case
        JSONObject(manager.call(validPingJson()))
        // Error case
        JSONObject(manager.call("bad json"))
    }

    @Test
    fun `call response for VALIDATION_ERROR has empty requestId when parse failed`() {
        val manager = BridgeManager()
        val result = parseResponse(manager.call("not json at all"))

        // requestId cannot be extracted from invalid JSON, so it remains ""
        assertEquals("", result.getString("requestId"))
    }

    // endregion

    // region default constructor behaviour

    @Test
    fun `default constructor allows only system ping`() {
        val manager = BridgeManager()

        // system.ping is allowed by default
        val pingResult = parseResponse(manager.call("""{"namespace":"system","method":"ping","requestId":"req_default_1"}"""))
        assertTrue(pingResult.getBoolean("ok"))

        // other methods are denied by default
        val otherResult = parseResponse(manager.call("""{"namespace":"system","method":"other","requestId":"req_default_2"}"""))
        assertFalse(otherResult.getBoolean("ok"))
        assertEquals("PERMISSION_DENIED", otherResult.getJSONObject("error").getString("code"))
    }

    // endregion
}