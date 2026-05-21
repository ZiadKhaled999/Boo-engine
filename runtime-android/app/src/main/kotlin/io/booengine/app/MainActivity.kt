package io.booengine.app

import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.activity.ComponentActivity
import io.booengine.core.AppLoader
import io.booengine.webview.WebViewConfigurator
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    private lateinit var webView: WebView
    private lateinit var loadingOverlay: View
    private val appLoader = AppLoader()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)
        loadingOverlay = findViewById(R.id.loading_overlay)

        WebViewConfigurator(appLoader).configure(webView, loadingOverlay)
        webView.addJavascriptInterface(BooBridge(), "booBridge")

        if (savedInstanceState == null) {
            webView.loadUrl(appLoader.resolveStartUrl(intent.getStringExtra(EXTRA_DEV_URL)))
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
                val outcome = JSONObject()
                    .put("ok", true)
                    .put("data", JSONObject().put("ack", true))
                    .put("requestId", requestId)
                Log.i(TAG, "requestId=$requestId namespace=$namespace method=$method duration=${System.currentTimeMillis()-start} outcome=ok")
                outcome.toString()
            } catch (err: Exception) {
                JSONObject()
                    .put("ok", false)
                    .put("error", JSONObject()
                        .put("code", "VALIDATION_ERROR")
                        .put("message", "Invalid bridge payload"))
                    .toString()
            }
        }
    }

    companion object {
        private const val TAG = "BooMainActivity"
        private const val EXTRA_DEV_URL = "boo.devUrl"
    }
}
