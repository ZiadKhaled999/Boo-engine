package io.booengine.app

import kotlin.test.Test
import kotlin.test.assertEquals

class BooConfigLoaderTest {

    @Test
    fun `uses explicit start url from config`() {
        val config = BooConfigLoader.fromJsonString(
            """{"appId":"io.boo.demo","appName":"Demo","startUrl":"file:///android_asset/demo/index.html"}""",
        )

        assertEquals("io.boo.demo", config.appId)
        assertEquals("Demo", config.appName)
        assertEquals("file:///android_asset/demo/index.html", config.startUrl)
        assertEquals(emptyList(), config.permissions)
    }

    @Test
    fun `falls back to defaults when fields missing`() {
        val config = BooConfigLoader.fromJsonString("{}")

        assertEquals("io.booengine.app", config.appId)
        assertEquals("Boo Engine", config.appName)
        assertEquals("file:///android_asset/index.html", config.startUrl)
        assertEquals(emptyList(), config.permissions)
    }

    @Test
    fun `parses permissions array`() {
        val config = BooConfigLoader.fromJsonString(
            """{"permissions":["app:read:metadata","app:read:permissions"]}""",
        )

        assertEquals(listOf("app:read:metadata", "app:read:permissions"), config.permissions)
    }
}
