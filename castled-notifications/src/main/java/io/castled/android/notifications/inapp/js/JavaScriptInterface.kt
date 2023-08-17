package com.example.javascriptsample

import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import io.castled.android.notifications.inapp.views.InAppWebViewLayout
import org.json.JSONObject

class JavaScriptInterface(private val layout: InAppWebViewLayout, private val webView: WebView) {

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

    fun setupWebViewClient() {
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
            val inputStream = layout.context.assets.open(filePath)
            val buffer = ByteArray(inputStream.available())
            inputStream.read(buffer)
            inputStream.close()
            val jsCode = String(buffer)
            webView.evaluateJavascript(jsCode, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JavascriptInterface
    fun dismissMessage(message: String) {
        val jsonObject = JSONObject(message)
        println("event captured********* dismissMessage $jsonObject")
    }

    @JavascriptInterface
    fun openDeepLink(deepLinkURL: String, message: String) {
        val jsonObject = JSONObject(message)
//        val value1: String = jsonObject.getString("key1")
//        val value2: String = jsonObject.getString("key2")
//        val button_title: String = jsonObject.getString("button_title")
        println("event captured********* openDeepLink $deepLinkURL  $jsonObject")


    }

    @JavascriptInterface
    fun navigateToScreen(screenName: String, message: String) {
        val jsonObject = JSONObject(message)
        println("event captured********* navigateToScreen $screenName  $jsonObject")

    }

    @JavascriptInterface
    fun openRichLanding(richLandingURL: String, message: String) {
        val jsonObject = JSONObject(message)
//        val value1: String = jsonObject.getString("key1")
//        val value2: String = jsonObject.getString("key2")
//        val button_title: String = jsonObject.getString("button_title")
        println("event captured********* openRichLanding $richLandingURL  $jsonObject")

    }

    @JavascriptInterface
    fun requestPushPermission(message: String) {
        val jsonObject = JSONObject(message)
        println("event captured********* requestPushPermission  $jsonObject")

    }

    @JavascriptInterface
    fun customAction(message: String) {
        val jsonObject = JSONObject(message)
        println("event captured********* customAction  $jsonObject")

    }


}