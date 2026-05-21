package io.booengine.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import io.booengine.app.bridge.BooBridge
import io.booengine.app.bridge.BridgeManager

private const val CONFIG_ASSET_PATH = "boo.config.json"

class MainActivity : ComponentActivity() {

    private lateinit var webView: WebView
    private lateinit var loadingOverlay: View

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)
        loadingOverlay = findViewById(R.id.loading_overlay)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowFileAccessFromFileURLs = false
        webView.settings.allowUniversalAccessFromFileURLs = false

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                loadingOverlay.visibility = View.GONE
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: android.webkit.WebResourceError,
            ) {
                if (request.isForMainFrame) {
                    showBootFailurePage(error.description?.toString() ?: "Unknown loading error")
                }
            }
        }

        val config = readBooConfig()
        val bridgeManager = BridgeManager(
            appId = config.appId,
            appName = config.appName,
            allowedPermissions = config.permissions,
        )
        webView.addJavascriptInterface(BooBridge(bridgeManager), "BooNative")

        webView.loadUrl(config.startUrl)
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onPause() {
        webView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }

    private fun readBooConfig(): BooConfig {
        return runCatching {
            assets.open(CONFIG_ASSET_PATH).bufferedReader().use { reader ->
                BooConfigLoader.fromJsonString(reader.readText())
            }
        }.getOrElse {
            BooConfigLoader.fromJsonString("{}")
        }
    }

    private fun showBootFailurePage(message: String) {
        val escaped = message.replace("<", "&lt;").replace(">", "&gt;")
        val html = """
            <html><body style=\"font-family:sans-serif;background:#111;color:#fff;padding:24px;\">
            <h2>Boo startup failed</h2>
            <p>Unable to load packaged web assets.</p>
            <pre style=\"white-space:pre-wrap\">$escaped</pre>
            </body></html>
        """.trimIndent()
        webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "utf-8", null)
    }
}
