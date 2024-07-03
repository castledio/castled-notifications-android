package io.castled.android.notifications.inapp.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import io.castled.android.notifications.R
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.store.models.Campaign
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

class InAppWebViewLayout(context: Context, attrs: AttributeSet) :
    InAppBaseViewLayout(context, attrs) {

    override val webView: WebView?
        get() = findViewById(R.id.castled_inapp_webview)
    override val viewContainer: View? = null
    override val headerView: TextView? = null
    override val messageView: TextView? = null
    override val imageView: ImageView? = null
    override val buttonViewContainer: View? = null
    override val primaryButton: Button? = null
    override val secondaryButton: Button? = null
    override val closeButton: ImageButton?
        get() = findViewById(R.id.castled_inapp_html_close_btn)


    override fun updateViewParams(inAppMessage: Campaign) {
        (inAppMessage.message["modal"]?.jsonObject
            ?: inAppMessage.message["fs"]?.jsonObject)?.let {
            updateCloseButtonVisibility(it)
        }

    }

    private fun updateCloseButtonVisibility(modalParams: JsonObject) {
        closeButton?.let { button ->
            button.visibility =
                if (InAppViewUtils.shouldShowCloseButton(modalParams)) View.VISIBLE else View.GONE
        }
    }

    companion object {
        val logger = CastledLogger.getInstance(LogTags.IN_APP)
    }
}