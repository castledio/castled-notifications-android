package io.castled.android.notifications.inapp.views

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import io.castled.android.notifications.R
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.store.models.Campaign
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

class InAppBannerViewLayout(context: Context, attrs: AttributeSet) :
    InAppBaseViewLayout(context, attrs) {

    override val viewContainer: View?
        get() = findViewById(R.id.castled_inapp_banner_container)
    override val headerView: TextView? = null
    override val messageView: TextView?
        get() = findViewById(R.id.castled_inapp_banner_message)
    override val imageView: ImageView?
        get() = findViewById(R.id.castled_inapp_banner_img)
    override val buttonViewContainer: View? = null
    override val primaryButton: Button? = null
    override val secondaryButton: Button? = null
    override val closeButton: ImageButton?
        get() = findViewById(R.id.castled_inapp_banner_close_btn)

    override fun updateViewParams(inAppMessage: Campaign) {
        // TODO: testing with modal payload
        val modalParams = inAppMessage.message["banner"]?.jsonObject ?: run {
            InAppModalViewLayout.logger.debug("banner object not present in in-app message!")
            return
        }
        updateImageView(modalParams)
        updateMessageView(modalParams)
    }

    private fun updateImageView(modalParams: JsonObject) {
        val imageViewParams = InAppViewUtils.getImageViewParams(modalParams)
        if (imageViewParams != null && imageView != null) {
            Glide.with(imageView!!.context)
                .load(imageViewParams.imageUrl).placeholder(R.drawable.castled_placeholder)
                .into(imageView!!)
        }
    }

    private fun updateMessageView(modalParams: JsonObject) {
        val messageViewParams = InAppViewUtils.getMessageViewParams(modalParams)
        messageView?.apply {
            setBackgroundColor(Color.TRANSPARENT)
            setTextColor(parseColor(messageViewParams.fontColor, Color.BLACK))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, messageViewParams.fontSize)
            text = messageViewParams.message
        }
        // Same color as message bg
        viewContainer?.apply {
            val drawable = background as GradientDrawable
            drawable.setColor(parseColor(messageViewParams.backgroundColor, Color.WHITE))
        }
    }

    private fun parseColor(colorStr: String?, defaultColor: Int): Int {
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