package io.booengine.bridge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.json.JSONObject

class BridgeRequestTest {

    // region fromJson — happy path

    @Test
    fun `fromJson parses all fields correctly`() {
        val json = """{"namespace":"system","method":"ping","requestId":"req_001","payload":{"key":"value"}}"""
        val request = BridgeRequest.fromJson(json)

        assertEquals("system", request.namespace)
        assertEquals("ping", request.method)
        assertEquals("req_001", request.requestId)
        assertEquals("value", request.payload.getString("key"))
    }

    @Test
    fun `fromJson defaults requestId to empty string when absent`() {
        val json = """{"namespace":"system","method":"ping"}"""
        val request = BridgeRequest.fromJson(json)

        assertEquals("", request.requestId)
    }

    @Test
    fun `fromJson defaults payload to empty JSONObject when absent`() {
        val json = """{"namespace":"system","method":"ping","requestId":"req_002"}"""
        val request = BridgeRequest.fromJson(json)

        assertNotNull(request.payload)
        assertEquals(0, request.payload.length())
    }

    @Test
    fun `fromJson trims whitespace from namespace and method`() {
        val json = """{"namespace":"  system  ","method":"  ping  ","requestId":"req_003"}"""
        val request = BridgeRequest.fromJson(json)

        assertEquals("system", request.namespace)
        assertEquals("ping", request.method)
    }

    @Test
    fun `fromJson trims whitespace from requestId`() {
        val json = """{"namespace":"system","method":"ping","requestId":"  req_004  "}"""
        val request = BridgeRequest.fromJson(json)

        assertEquals("req_004", request.requestId)
    }

    @Test
    fun `fromJson accepts payload with nested objects`() {
        val json = """{"namespace":"system","method":"ping","payload":{"nested":{"a":1}}}"""
        val request = BridgeRequest.fromJson(json)

        assertNotNull(request.payload.getJSONObject("nested"))
        assertEquals(1, request.payload.getJSONObject("nested").getInt("a"))
    }

    // endregion

    // region fromJson — validation errors

    @Test(expected = BridgeValidationException::class)
    fun `fromJson throws BridgeValidationException for invalid JSON`() {
        BridgeRequest.fromJson("not valid json {{{")
    }

    @Test(expected = BridgeValidationException::class)
    fun `fromJson throws BridgeValidationException when namespace is absent`() {
        BridgeRequest.fromJson("""{"method":"ping"}""")
    }

    @Test(expected = BridgeValidationException::class)
    fun `fromJson throws BridgeValidationException when method is absent`() {
        BridgeRequest.fromJson("""{"namespace":"system"}""")
    }

    @Test(expected = BridgeValidationException::class)
    fun `fromJson throws BridgeValidationException when namespace is empty string`() {
        BridgeRequest.fromJson("""{"namespace":"","method":"ping"}""")
    }

    @Test(expected = BridgeValidationException::class)
    fun `fromJson throws BridgeValidationException when method is empty string`() {
        BridgeRequest.fromJson("""{"namespace":"system","method":""}""")
    }

    @Test(expected = BridgeValidationException::class)
    fun `fromJson throws BridgeValidationException when namespace is whitespace only`() {
        BridgeRequest.fromJson("""{"namespace":"   ","method":"ping"}""")
    }

    @Test(expected = BridgeValidationException::class)
    fun `fromJson throws BridgeValidationException when method is whitespace only`() {
        BridgeRequest.fromJson("""{"namespace":"system","method":"   "}""")
    }

    @Test(expected = BridgeValidationException::class)
    fun `fromJson throws BridgeValidationException for empty string input`() {
        BridgeRequest.fromJson("")
    }

    // endregion

    // region fromJson — payload size enforcement

    @Test(expected = BridgePayloadTooLargeException::class)
    fun `fromJson throws BridgePayloadTooLargeException when payload exceeds 32KB`() {
        val bigValue = "x".repeat(BridgeRequest.MAX_REQUEST_SIZE_BYTES + 1)
        val json = """{"namespace":"system","method":"ping","value":"$bigValue"}"""
        BridgeRequest.fromJson(json)
    }

    @Test
    fun `fromJson succeeds when payload is exactly at max size boundary`() {
        // Build a JSON that is exactly MAX_REQUEST_SIZE_BYTES bytes in UTF-8
        val prefix = """{"namespace":"system","method":"ping","v":""""
        val suffix = """"} """
        val fillSize = BridgeRequest.MAX_REQUEST_SIZE_BYTES - prefix.length - suffix.length
        // Only run this test if fillSize is non-negative (sanity guard)
        if (fillSize >= 0) {
            val json = prefix + "a".repeat(fillSize) + suffix
            assertTrue(json.toByteArray(Charsets.UTF_8).size <= BridgeRequest.MAX_REQUEST_SIZE_BYTES)
            val request = BridgeRequest.fromJson(json)
            assertEquals("system", request.namespace)
        }
    }

    @Test(expected = BridgePayloadTooLargeException::class)
    fun `fromJson throws BridgePayloadTooLargeException for multibyte UTF-8 that exceeds limit`() {
        // Each '€' character is 3 bytes in UTF-8, so 11000 of them = 33000 bytes
        val bigValue = "€".repeat(11000)
        val json = """{"namespace":"a","method":"b","v":"$bigValue"}"""
        BridgeRequest.fromJson(json)
    }

    // endregion

    // region methodKey property

    @Test
    fun `methodKey returns namespace dot method`() {
        val json = """{"namespace":"system","method":"ping"}"""
        val request = BridgeRequest.fromJson(json)

        assertEquals("system.ping", request.methodKey)
    }

    @Test
    fun `methodKey uses trimmed namespace and method values`() {
        val json = """{"namespace":"  api  ","method":"  query  "}"""
        val request = BridgeRequest.fromJson(json)

        assertEquals("api.query", request.methodKey)
    }

    // endregion

    // region data class properties

    @Test
    fun `BridgeRequest data class equality holds for identical instances`() {
        val payload = JSONObject()
        val r1 = BridgeRequest("system", "ping", payload, "req_001")
        val r2 = BridgeRequest("system", "ping", payload, "req_001")

        assertEquals(r1, r2)
    }

    @Test
    fun `MAX_REQUEST_SIZE_BYTES constant is 32 KB`() {
        assertEquals(32 * 1024, BridgeRequest.MAX_REQUEST_SIZE_BYTES)
    }

    // endregion

    // region exception types

    @Test
    fun `BridgeValidationException is an IllegalArgumentException`() {
        val ex = BridgeValidationException("test")
        assertTrue(ex is IllegalArgumentException)
        assertEquals("test", ex.message)
    }

    @Test
    fun `BridgePayloadTooLargeException is an IllegalArgumentException`() {
        val ex = BridgePayloadTooLargeException("too large")
        assertTrue(ex is IllegalArgumentException)
        assertEquals("too large", ex.message)
    }

    // endregion
}