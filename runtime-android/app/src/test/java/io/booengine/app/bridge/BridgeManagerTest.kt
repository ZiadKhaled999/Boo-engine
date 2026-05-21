package io.booengine.app.bridge

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BridgeManagerTest {

    @Test
    fun `returns metadata for valid app plugin request`() {
        val manager = BridgeManager(
            appId = "io.booengine.demo",
            appName = "Boo Demo",
            allowedPermissions = listOf("app:read:metadata"),
        )

        val rawResponse = manager.handle(
            """{"requestId":"req-1","plugin":"app","method":"getMetadata","payload":{}}""",
        )

        val response = BridgeResponse.fromJsonString(rawResponse)
        assertEquals("req-1", response.requestId)
        assertTrue(response.ok)
        assertEquals("io.booengine.demo", response.result?.optString("appId"))
        assertEquals("Boo Demo", response.result?.optString("appName"))
    }

    @Test
    fun `returns permission denied when config lacks permission`() {
        val manager = BridgeManager(
            appId = "io.booengine.demo",
            appName = "Boo Demo",
            allowedPermissions = emptyList(),
        )

        val rawResponse = manager.handle(
            """{"requestId":"req-2","plugin":"app","method":"getMetadata","payload":{}}""",
        )

        val response = BridgeResponse.fromJsonString(rawResponse)
        assertFalse(response.ok)
        assertEquals("PERMISSION_DENIED", response.error?.code)
    }
}
