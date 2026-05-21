package io.booengine.app

import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import io.booengine.bridge.BridgeManager

class MainActivity : ComponentActivity() {
    private lateinit var webView: WebView
    private lateinit var loadingOverlay: View
    private val bridgeManager = BridgeManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)
        loadingOverlay = findViewById(R.id.loading_overlay)

        configureWebView(webView)
        webView.addJavascriptInterface(bridgeManager, "booBridge")

        if (savedInstanceState == null) {
            webView.loadUrl(resolveStartUrl())
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

    private fun configureWebView(target: WebView) {
        target.webChromeClient = WebChromeClient()
        target.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                loadingOverlay.visibility = View.GONE
            }
        }

        target.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            allowFileAccess = true
            allowContentAccess = true
            mediaPlaybackRequiresUserGesture = false
        }
    }

    private fun resolveStartUrl(): String {
        val devUrl = intent.getStringExtra(EXTRA_DEV_URL)?.trim().orEmpty()
        return if (devUrl.startsWith("http://") || devUrl.startsWith("https://")) devUrl else LOCAL_ASSET_URL
    }

    companion object {
        private const val LOCAL_ASSET_URL = "file:///android_asset/index.html"
        private const val EXTRA_DEV_URL = "boo.devUrl"
    }
}
