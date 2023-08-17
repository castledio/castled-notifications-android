package io.castled.android.notifications.inapp.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Base64
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.example.javascriptsample.JavaScriptInterface
import io.castled.android.notifications.R
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import java.nio.charset.StandardCharsets

class InAppWebViewLayout(context: Context, attrs: AttributeSet) :
    InAppBaseViewLayout(context, attrs) {

    private val webView: WebView?
        get() = findViewById(R.id.castled_inapp_webview)
    override val viewContainer: View? = null
    override val headerView: TextView?
        get() = findViewById(R.id.castled_inapp_fs_title)
    override val messageView: TextView?
        get() = findViewById(R.id.castled_inapp_fs_message)
    override val imageView: ImageView?
        get() = findViewById(R.id.castled_inapp_fs_img)
    override val buttonViewContainer: View?
        get() = findViewById(R.id.castled_inapp_fs_btn_container)
    override val primaryButton: Button?
        get() = findViewById(R.id.castled_inapp_fs_btn_primary)
    override val secondaryButton: Button?
        get() = findViewById(R.id.castled_inapp_fs_btn_secondary)
    override val closeButton: ImageButton?
        get() = findViewById(R.id.castled_inapp_fs_close_btn)


    override fun updateViewParams(message: JsonObject) {
        val htmlString =
            (message["modal"]?.jsonObject ?: message["fs"]?.jsonObject)?.get("html")?.toString()
                ?: run {
                    logger.debug("Modal/ FS object not present in in-app message!")
                    return
                }
        updateWebview(htmlString)
    }

    private fun updateWebview(htmlString: String) {


        val decodedHtmlString: String = try {
            val decodedBytes = Base64.decode(htmlString, Base64.DEFAULT)
            String(decodedBytes, Charsets.UTF_8)
        } catch (e: IllegalArgumentException) {
            htmlString // Use original encoded string if decoding fails
        }


        val jsinterface = JavaScriptInterface(this, webView!!)
        webView?.loadDataWithBaseURL(
            null, decodedHtmlString, "text/html",
            "utf-8", null
        )
        jsinterface.setupWebViewClient()

    }

    private fun parseColor(colorStr: String, defaultColor: Int): Int {
        return try {
            Color.parseColor(colorStr)
        } catch (e: IllegalArgumentException) {
            defaultColor
        }
    }

    companion object {
        val logger = CastledLogger.getInstance(LogTags.IN_APP)
    }
}