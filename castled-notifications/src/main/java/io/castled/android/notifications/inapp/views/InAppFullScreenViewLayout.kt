package io.castled.android.notifications.inapp.views

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Point
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.castled.android.notifications.R
import io.castled.android.notifications.commons.CastledUtils
import io.castled.android.notifications.commons.ColorUtils
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
    override val primaryButton: TextView?
        get() = findViewById(R.id.castled_inapp_fs_btn_primary)
    override val secondaryButton: TextView?
        get() = findViewById(R.id.castled_inapp_fs_btn_secondary)
    override val closeButton: ImageButton?
        get() = findViewById(R.id.castled_inapp_fs_close_btn)

    override fun updateViewParams(inAppMessage: Campaign) {
        val modalParams = inAppMessage.message["fs"]?.jsonObject ?: run {
            logger.debug("fs object not present in in-app message!")
            return
        }

        updateImageView(modalParams)
        updateHeaderView(modalParams)
        updateMessageView(modalParams)
        updatePrimaryBtnView(modalParams)
        updateSecondaryBtnView(modalParams)
        updateCloseButtonVisibility(modalParams)
        updateContentViewSizes()
    }

    private fun updateCloseButtonVisibility(modalParams: JsonObject) {
        closeButton?.let { button ->
            button.visibility =
                if (InAppViewUtils.shouldShowCloseButton(modalParams)) View.VISIBLE else View.GONE
        }
    }

    private fun updateImageView(modalParams: JsonObject) {
        val imageViewParams = InAppViewUtils.getImageViewParams(modalParams)
        if (imageViewParams != null && imageView != null) {
            Glide.with(imageView!!.context).load(imageViewParams.imageUrl).apply(
                RequestOptions()
                    .placeholder(R.drawable.castled_placeholder)
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
        (viewContainer as? RelativeLayout)?.setBackgroundColor(
            parseColor(
                messageViewParams.backgroundColor,
                Color.WHITE
            )
        )
    }

    private fun updatePrimaryBtnView(modalParams: JsonObject) {
        val primaryButtonViewParams = InAppViewUtils.getPrimaryButtonViewParams(modalParams)
            ?: run {
                primaryButton?.let {
                    primaryButton!!.visibility = GONE
                }
                return
            }
        primaryButton?.apply {
            setTextColor(parseColor(primaryButtonViewParams.fontColor, Color.WHITE))
            CastledUtils.changeBackgroundColorAndBorderColor(
                primaryButton!!,
                parseColor(primaryButtonViewParams.buttonColor, Color.BLUE),
                parseColor(primaryButtonViewParams.borderColor, Color.TRANSPARENT)
            )
            text = primaryButtonViewParams.buttonText
        }
    }

    private fun updateSecondaryBtnView(modalParams: JsonObject) {
        val secondaryButtonViewParams = InAppViewUtils.getSecondaryButtonViewParams(modalParams)
            ?: run {
                buttonViewContainer?.let {
                    buttonViewContainer!!.visibility = GONE
                }
                return
            }
        secondaryButton?.apply {
            setTextColor(parseColor(secondaryButtonViewParams.fontColor, Color.BLACK))
            CastledUtils.changeBackgroundColorAndBorderColor(
                secondaryButton!!,
                parseColor(secondaryButtonViewParams.buttonColor, Color.WHITE),
                parseColor(secondaryButtonViewParams.borderColor, Color.TRANSPARENT)
            )
            text = secondaryButtonViewParams.buttonText
        }
    }

    private fun parseColor(colorStr: String, defaultColor: Int): Int {
        return ColorUtils.parseColor(colorStr, defaultColor)
    }

    private fun updateContentViewSizes() {

        val screenSize = CastledUtils.getScreenSize(context)
        val dialogSize = Point(
            (screenSize.x),
            (screenSize.y)
        )
        val orientation =
            CastledUtils.getCurrentOrientation(context) // Assuming you are inside an activity
        var messageViewMaxLines = 0
        var headerViewMaxLines = 0

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Device is in portrait orientation
            imageView!!.layoutParams.height = (dialogSize.x * 3 / 4)
            headerViewMaxLines = 3
            messageViewMaxLines = 8


        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Device is in landscape orientation
            imageView!!.layoutParams.height = (dialogSize.x * 1 / 5)
            messageViewMaxLines = 2
            headerViewMaxLines = 2
        }
        messageView!!.maxLines = messageViewMaxLines
        headerView!!.maxLines = headerViewMaxLines
    }

    companion object {
        val logger = CastledLogger.getInstance(LogTags.IN_APP)
    }

}