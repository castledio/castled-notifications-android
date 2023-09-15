package io.castled.android.notifications.inapp.js
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import io.castled.android.notifications.commons.ClickActionParams
import io.castled.android.notifications.inapp.InAppViewDecorator
import io.castled.android.notifications.inapp.views.InAppViewUtils
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

internal class JavaScriptInterface(
    private val decorator: InAppViewDecorator,
    private val webView: WebView
) {
    var eventListener: ((eventData: ClickActionParams) -> Unit)? = null

    init {
        setupWebView()

    }

    private fun setupWebView() {
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        webView.addJavascriptInterface(this, "castledBridgeInternal")
    }

    internal fun setupWebViewClient() {
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                injectJavaScriptClass()
            }
        }
    }

    private var isJavaScriptInjected = false
    private fun injectJavaScriptClass() {

        if (isJavaScriptInjected)
            return
        isJavaScriptInjected = true
        loadAndExecuteJavaScriptFile(webView, "castled_bridge.js")

    }

    private fun loadAndExecuteJavaScriptFile(webView: WebView, filePath: String) {
        try {
            val inputStream = decorator.context.assets.open(filePath)
            val buffer = ByteArray(inputStream.available())
            inputStream.read(buffer)
            inputStream.close()
            val jsCode = String(buffer)
            webView.evaluateJavascript(jsCode, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    internal fun setEventListener(listener: (eventData: ClickActionParams) -> Unit) {
        eventListener = listener
    }

    @JavascriptInterface
    fun onButtonClicked(message: String) {
        val jsonObject = Json.parseToJsonElement(message).jsonObject
        val eventParams = InAppViewUtils.getWebViewButtonActionParams(jsonObject)
        if (eventParams != null) {
            eventListener?.invoke(eventParams)
        }

    }
}