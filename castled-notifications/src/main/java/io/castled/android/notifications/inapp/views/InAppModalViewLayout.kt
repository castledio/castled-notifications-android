package io.castled.android.notifications.inapp.views

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import io.castled.android.notifications.R
import io.castled.android.notifications.commons.CastledUtils
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.store.models.Campaign
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

class InAppModalViewLayout(context: Context, attrs: AttributeSet) :
    InAppBaseViewLayout(context, attrs) {

    override val viewContainer: View?
        get() = findViewById(R.id.castled_inapp_modal_container)
    override val headerView: TextView?
        get() = findViewById(R.id.castled_inapp_modal_title)
    override val messageView: TextView?
        get() = findViewById(R.id.castled_inapp_modal_message)
    override val imageView: ImageView?
        get() = findViewById(R.id.castled_inapp_modal_img)
    override val buttonViewContainer: View?
        get() = findViewById(R.id.castled_inapp_modal_btn_container)
    override val primaryButton: TextView?
        get() = findViewById(R.id.castled_inapp_modal_btn_primary)
    override val secondaryButton: TextView?
        get() = findViewById(R.id.castled_inapp_modal_btn_secondary)
    override val closeButton: ImageButton?
        get() = findViewById(R.id.castled_inapp_modal_close_btn)

    override fun updateViewParams(inAppMessage: Campaign) {
        val modalParams = inAppMessage.message["modal"]?.jsonObject ?: run {
            logger.debug("Model object not present in in-app message!")
            return
        }
        updateImageView(modalParams)
        updateHeaderView(modalParams)
        updateMessageView(modalParams)
        updatePrimaryBtnView(modalParams)
        updateSecondaryBtnView(modalParams)
        updateContentViewSizes()

    }

    fun updateContentViewSizes() {
        val dialoguePercentage =
            context.resources.getString(R.string.castled_inapp_dialouge_size).toFloat()
        val screenSize = CastledUtils.getScreenSize(context)
        val dialogSize = Point(
            (screenSize.x * dialoguePercentage).toInt(),
            (screenSize.y * dialoguePercentage).toInt()
        )
        val orientation =
            CastledUtils.getCurrentOrientation(context) // Assuming you are inside an activity
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Device is in portrait orientation
            imageView!!.layoutParams.height = (dialogSize.x * 3 / 4).toInt()

        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Device is in landscape orientation
            imageView!!.layoutParams.height = (dialogSize.x * 1 / 5).toInt()
        }
        val remainingHeight = dialogSize.y - imageView!!.layoutParams.height
        headerView!!.maxHeight = remainingHeight * 1 / 4
        messageView!!.maxHeight = remainingHeight * 2 / 4
        headerView!!.maxLines = 2
        messageView!!.maxLines = 5

        // remaining 1/4th for buttons


    }

    private fun updateImageView(modalParams: JsonObject) {
        val imageViewParams = InAppViewUtils.getImageViewParams(modalParams)
        if (imageViewParams != null && imageView != null) {
            Glide.with(imageView!!.context)
                .load(imageViewParams.imageUrl).placeholder(R.drawable.castled_placeholder)
                .into(imageView!!)
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
            setTextColor(parseColor(messageViewParams.fontColor, Color.BLACK))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, messageViewParams.fontSize)
            text = messageViewParams.message
        }
        // Message area and button area color will be covered with this
        viewContainer?.apply {
            val drawable = when (background) {
                is GradientDrawable -> {
                    (background as GradientDrawable).setColor(
                        parseColor(
                            messageViewParams.backgroundColor,
                            Color.WHITE
                        )
                    )
                    null
                }

                is ColorDrawable -> {
                    (background as ColorDrawable).color =
                        parseColor(messageViewParams.backgroundColor, Color.WHITE)

                    null
                }

                else -> {}
            }
        }
    }

    private fun updatePrimaryBtnView(modalParams: JsonObject) {
        val primaryButtonViewParams = InAppViewUtils.getPrimaryButtonViewParams(modalParams)
            ?: return
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
            ?: return
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