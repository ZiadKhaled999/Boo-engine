package io.booengine.app

import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.activity.ComponentActivity
import io.booengine.config.BooConfig
import io.booengine.core.AppLoader
import io.booengine.webview.WebViewConfigurator
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    private lateinit var webView: WebView
    private lateinit var loadingOverlay: View
    private lateinit var booConfig: BooConfig
    private val appLoader = AppLoader()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        booConfig = BooConfig.fromAssets(this)
        webView = findViewById(R.id.webview)
        loadingOverlay = findViewById(R.id.loading_overlay)

        WebViewConfigurator(appLoader).configure(webView, loadingOverlay)
        webView.addJavascriptInterface(BooBridge(), "booBridge")

        if (savedInstanceState == null) {
            webView.loadUrl(appLoader.resolveStartUrl(intent.getStringExtra(EXTRA_DEV_URL) ?: booConfig.startUrl))
        }
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
        webView.resumeTimers()
    }

    override fun onPause() {
        webView.onPause()
        webView.pauseTimers()
        super.onPause()
    }

    override fun onDestroy() {
        webView.removeJavascriptInterface("booBridge")
        webView.destroy()
        super.onDestroy()
    }

    private inner class BooBridge {
        @JavascriptInterface
        fun call(requestJson: String): String {
            val start = System.currentTimeMillis()
            return try {
                val body = JSONObject(requestJson)
                val namespace = body.optString("namespace")
                val method = body.optString("method")
                val requestId = body.optString("requestId", "")
                val permission = permissionFor(namespace, method)

                val response = when {
                    !booConfig.isPluginEnabled(namespace) -> error("PERMISSION_DENIED", "Plugin is disabled", requestId)
                    permission == null -> error("METHOD_NOT_FOUND", "Unknown method", requestId)
                    !booConfig.hasPermission(permission) -> error("PERMISSION_DENIED", "Permission is not granted", requestId)
                    namespace == "app" && method == "readMetadata" -> success(booConfig.asMetadataJson(), requestId)
                    namespace == "app" && method == "readPermissions" -> success(booConfig.asPermissionsJson(), requestId)
                    else -> error("METHOD_NOT_FOUND", "Unknown method", requestId)
                }

                Log.i(TAG, "requestId=$requestId namespace=$namespace method=$method duration=${System.currentTimeMillis()-start} outcome=${if (JSONObject(response).optBoolean("ok")) "ok" else "error"}")
                response
            } catch (_: Exception) {
                error("VALIDATION_ERROR", "Invalid bridge payload", "")
            }
        }

        private fun permissionFor(namespace: String, method: String): String? {
            return when ("$namespace:$method") {
                "app:readMetadata" -> "app:read:metadata"
                "app:readPermissions" -> "app:read:permissions"
                else -> null
            }
        }

        private fun success(data: Any, requestId: String): String = JSONObject()
            .put("ok", true)
            .put("data", data)
            .put("requestId", requestId)
            .toString()

        private fun error(code: String, message: String, requestId: String): String = JSONObject()
            .put("ok", false)
            .put("error", JSONObject().put("code", code).put("message", message))
            .put("requestId", requestId)
            .toString()
    }

    companion object {
        private const val TAG = "BooMainActivity"
        private const val EXTRA_DEV_URL = "boo.devUrl"
    }
}
