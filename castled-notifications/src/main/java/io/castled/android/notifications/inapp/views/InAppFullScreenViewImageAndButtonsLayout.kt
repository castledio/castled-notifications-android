package io.castled.android.notifications.inapp.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
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

class InAppFullScreenViewImageAndButtonsLayout(context: Context, attrs: AttributeSet) :
    InAppBaseViewLayout(context, attrs) {

    override val viewContainer: View?
        get() = findViewById(R.id.castled_inapp_fs_container)
    override val headerView: TextView? = null
    override val messageView: TextView? = null
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
        updatePrimaryBtnView(modalParams)
        updateSecondaryBtnView(modalParams)
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

    companion object {
        val logger = CastledLogger.getInstance(LogTags.IN_APP)
    }

}