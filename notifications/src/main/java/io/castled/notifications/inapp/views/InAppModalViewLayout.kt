package io.castled.notifications.inapp.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.castled.notifications.R
import io.castled.notifications.logger.CastledLogger
import io.castled.notifications.logger.LogTags
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

class InAppModalViewLayout(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

    val viewContainer: View?
        get() = findViewById(R.id.castled_inapp_modal_container)
    private val headerTextView: TextView?
        get() = findViewById(R.id.castled_inapp_modal_title)
    private val messageView: TextView?
        get() = findViewById(R.id.castled_inapp_modal_message)
    private val modalImageView: ImageView?
        get() = findViewById(R.id.castled_inapp_modal_img)
    private val buttonViewContainer: View?
        get() = findViewById(R.id.castled_inapp_modal_btn_container)
    val primaryButton: Button?
        get() = findViewById(R.id.castled_inapp_modal_btn_primary)
    val secondaryButton: Button?
        get() = findViewById(R.id.castled_inapp_modal_btn_secondary)
    val closeButton: ImageButton?
        get() = findViewById(R.id.castled_inapp_modal_close_btn)


    fun updateViewParams(message: JsonObject) {
        val modalParams = message["modal"]?.jsonObject ?: run {
            logger.debug("Model object not present in in-app message!")
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
        if (imageViewParams != null && modalImageView != null) {
            Glide.with(modalImageView!!.context).load(imageViewParams.imageUrl).apply(
                RequestOptions()
                    .placeholder(0)
                    .error(0)
            ).into(modalImageView!!)
        }
    }

    private fun updateHeaderView(modalParams: JsonObject) {
        val headerViewParams = InAppViewUtils.getHeaderViewParams(modalParams)
        headerTextView?.apply {
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