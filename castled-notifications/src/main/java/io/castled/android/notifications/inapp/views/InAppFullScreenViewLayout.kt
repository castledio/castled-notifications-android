package io.castled.android.notifications.inapp.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.castled.android.notifications.R
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.store.models.Campaign
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

class InAppFullScreenViewLayout(context: Context, attrs: AttributeSet) :
    InAppBaseViewLayout(context, attrs) {

    override val viewContainer: View?
        get() = findViewById(R.id.castled_inapp_fs_container)
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

    override fun updateViewParams(inAppMessage: Campaign) {
        // TODO: testing with modal payload
        val modalParams = inAppMessage.message["fs"]?.jsonObject ?: run {
            InAppModalViewLayout.logger.debug("fs object not present in in-app message!")
            return
        }

        updateImageView(modalParams)
        updateHeaderView(modalParams)
        updateMessageView(modalParams)
        updatePrimaryBtnView(modalParams)
        updateSecondaryBtnView(modalParams)
    }

    private fun updateImageView(modalParams: JsonObject) {
        val imageViewParams = InAppViewUtils.getImageViewParams(modalParams)
        if (imageViewParams != null && imageView != null) {
            Glide.with(imageView!!.context).load(imageViewParams.imageUrl).apply(
                RequestOptions()
                    .placeholder(0)
                    .error(0)
            ).into(imageView!!)
        }
    }

    private fun updateHeaderView(modalParams: JsonObject) {
        val headerViewParams = InAppViewUtils.getHeaderViewParams(modalParams)
        headerView?.apply {
            setBackgroundColor(parseColor(headerViewParams.backgroundColor, Color.WHITE))
            setTextColor(parseColor(headerViewParams.fontColor, Color.BLACK))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, headerViewParams.fontSize)
            text = headerViewParams.header
        }
    }

    private fun updateMessageView(modalParams: JsonObject) {
        val messageViewParams = InAppViewUtils.getMessageViewParams(modalParams)
        messageView?.apply {
            setBackgroundColor(parseColor(messageViewParams.backgroundColor, Color.WHITE))
            setTextColor(parseColor(messageViewParams.fontColor, Color.BLACK))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, messageViewParams.fontSize)
            text = messageViewParams.message
        }
        // Btn panel also have same color as the message section
        buttonViewContainer?.apply {
            setBackgroundColor(parseColor(messageViewParams.backgroundColor, Color.WHITE))
        }
    }

    private fun updatePrimaryBtnView(modalParams: JsonObject) {
        val primaryButtonViewParams = InAppViewUtils.getPrimaryButtonViewParams(modalParams)
            ?: return
        primaryButton?.apply {
            setTextColor(parseColor(primaryButtonViewParams.fontColor, Color.WHITE))
            setBackgroundColor(parseColor(primaryButtonViewParams.buttonColor, Color.BLUE))
            text = primaryButtonViewParams.buttonText
        }
    }

    private fun updateSecondaryBtnView(modalParams: JsonObject) {
        val secondaryButtonViewParams = InAppViewUtils.getSecondaryButtonViewParams(modalParams)
            ?: return
        secondaryButton?.apply {
            setTextColor(parseColor(secondaryButtonViewParams.fontColor, Color.BLACK))
            setBackgroundColor(parseColor(secondaryButtonViewParams.buttonColor, Color.BLACK))
            text = secondaryButtonViewParams.buttonText
        }
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