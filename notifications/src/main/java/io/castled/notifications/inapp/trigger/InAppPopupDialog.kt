package io.castled.notifications.inapp.trigger

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import io.castled.notifications.inapp.models.consts.InAppConstants
import io.castled.notifications.store.models.Campaign
import io.castled.notifications.R
import io.castled.notifications.logger.CastledLogger
import io.castled.notifications.logger.LogTags
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

internal class InAppPopupDialog {

    companion object {

        private val logger = CastledLogger.getInstance(
            LogTags.IN_APP_POP_UP)

        internal fun showDialog(
            context: Context,
            autoDismissInterval: Long,
            popUpBackgroundColor: String,
            popUpHeader: PopupHeader,
            popupMessage: PopupMessage,
            imageUrl: String,
            popupPrimaryButton: PopupPrimaryButton,
            popupSecondaryButton: PopupSecondaryButton,
            inAppClickAction: InAppClickAction
        ) {
            val dialog = Dialog(context)
//            val dialog = Dialog(context, R.style.event_dialog_style)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.dialog_popup_triggered)


            // e.g. top + right margins:
//            dialog.window!!.setGravity(Gravity.TOP or Gravity.RIGHT)
//            val layoutParams = dialog.window!!.attributes
//            layoutParams.x = 100 // right margin
//
//            layoutParams.y = 170 // top margin
//
//            dialog.window!!.attributes = layoutParams


// e.g. bottom + left margins:
//            dialog.window!!.setGravity(Gravity.BOTTOM or Gravity.LEFT)
//            val layoutParams = dialog.window!!.attributes
//            layoutParams.x = 100 // left margin
//
//            layoutParams.y = 170 // bottom margin
//
//            dialog.window!!.attributes = layoutParams


            /*val lp = WindowManager.LayoutParams()
            lp.copyFrom(dialog.window?.attributes)
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.MATCH_PARENT*/


            // The below 3 lines is commented, because "Screen Overlay" color is not for the background of Dialog
            // but "Screen Overlay" is for transparent color for the background activity/fragment.
            // will handle this when, we get some more clearance.
//            val frameLayout: RelativeLayout = dialog.findViewById(R.id.frame_layout_root)
//            val gradientDrawable: GradientDrawable = frameLayout.background as GradientDrawable
//            gradientDrawable.setColor(Color.parseColor(returnDefaultOrValidHexColor(popUpBackgroundColor, "#FFFFFF")))

            val view: View = dialog.findViewById(R.id.view_close)
            view.setOnClickListener {

                inAppClickAction.onTrigger(
                    InAppConstants.Companion.EventClickType.CLOSE_EVENT
                )

                dialog.dismiss()
            }

//            https://img.uswitch.com/qhi9fkhtpbo3/hSSkIfF0OsQQGuiCCm0EQ/6c1a9b54de813e0a71a85edb400d58d8/rsz_1android.jpg
            //https://upload.wikimedia.org/wikipedia/commons/thumb/7/77/Google_Images_2015_logo.svg/800px-Google_Images_2015_logo.svg.png
            // TODO: close gitHub(Image scaling issue)-> https://github.com/dheerajbhaskar/castled-notifications-android/issues/54
            val imageView: ImageView = dialog.findViewById(R.id.img_popup)
            Glide.with(context).load(imageUrl)
//                .centerCrop()
//                .centerInside()
                .fitCenter()
                .into(imageView)
            imageView.setOnClickListener {

                inAppClickAction.onTrigger(
                    InAppConstants.Companion.EventClickType.IMAGE_CLICK
                )


                /*if (urlForOnClickOnImage.isNotEmpty()){
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlForOnClickOnImage))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    if (intent.resolveActivity(context.packageManager) != null)
                        context.startActivity(intent)
                }
                */
                // TODO: close gitHub(on image top dialog will not close)-> https://github.com/dheerajbhaskar/castled-notifications-android/issues/54
                //below line commented because the Dialog not need to close when tapping on the image
//                dialog.dismiss()
            }

            val textHeader: TextView = dialog.findViewById(R.id.txt_header)
            textHeader.setBackgroundColor(
                Color.parseColor(
                    returnDefaultOrValidHexColor(
                        popUpHeader.backgroundColor,
                        "#FFFFFF"
                    )
                )
            )
            textHeader.setTextColor(
                Color.parseColor(
                    returnDefaultOrValidHexColor(
                        popUpHeader.fontColor,
                        "#000000"
                    )
                )
            )
            textHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, popUpHeader.fontSize)
            textHeader.text = popUpHeader.header

            val textMessage: TextView = dialog.findViewById(R.id.txt_message)
            textMessage.setBackgroundColor(
                Color.parseColor(
                    returnDefaultOrValidHexColor(
                        popupMessage.backgroundColor,
                        "#FFFFFF"
                    )
                )
            )
            textMessage.setTextColor(
                Color.parseColor(
                    returnDefaultOrValidHexColor(
                        popupMessage.fontColor,
                        "#000000"
                    )
                )
            )
            textMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, popupMessage.fontSize)
            textMessage.text = popupMessage.message

            val gradientDrawableButtonPrimary = GradientDrawable()
            gradientDrawableButtonPrimary.setColor(
                Color.parseColor(
                    returnDefaultOrValidHexColor(
                        popupPrimaryButton.buttonColor,
                        "#FFFFFF"
                    )
                )
            )
            gradientDrawableButtonPrimary.cornerRadius = 10f
            gradientDrawableButtonPrimary.setStroke(
                2,
                Color.parseColor(
                    returnDefaultOrValidHexColor(
                        popupPrimaryButton.borderColor,
                        "#FFFFFF"
                    )
                )
            )

            val btnPrimary: TextView = dialog.findViewById(R.id.btn_primary)
            btnPrimary.background = gradientDrawableButtonPrimary
            btnPrimary.setTextColor(
                Color.parseColor(
                    returnDefaultOrValidHexColor(
                        popupPrimaryButton.fontColor,
                        "#000000"
                    )
                )
            )
            btnPrimary.text = popupPrimaryButton.buttonText
            btnPrimary.setOnClickListener {

                inAppClickAction.onTrigger(
                    InAppConstants.Companion.EventClickType.PRIMARY_BUTTON
                )

                /* if (popupPrimaryButton.urlOnClick.isNotEmpty()){
                     logger.debug("PrimaryButton performing onClick action. ${popupPrimaryButton.urlOnClick}")
                     val intent = Intent(Intent.ACTION_VIEW, Uri.parse(popupPrimaryButton.urlOnClick))
                     //The below line is to test.
 //                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https:www.google.com/"))
                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                     if (intent.resolveActivity(context.packageManager) != null)
                         context.startActivity(intent)
                     else {
 //                        Toast.makeText(context, "No application found to process the request.", Toast.LENGTH_LONG).show()
                         logger.debug("showDialog: No application found to process the request.")
                     }
                 } else {
 //                    Toast.makeText(context, "Not able to handle the request.", Toast.LENGTH_LONG).show()
                     logger.debug("showDialog: Not able to handle the request.")
                 }*/

                dialog.dismiss()
            }

            val gradientDrawableButtonSecondary = GradientDrawable()
            gradientDrawableButtonSecondary.setColor(
                Color.parseColor(
                    returnDefaultOrValidHexColor(
                        popupSecondaryButton.buttonColor,
                        "#FFFFFF"
                    )
                )
            )
            gradientDrawableButtonSecondary.cornerRadius = 10f
            gradientDrawableButtonSecondary.setStroke(
                2,
                Color.parseColor(
                    returnDefaultOrValidHexColor(
                        popupSecondaryButton.borderColor,
                        "#FFFFFF"
                    )
                )
            )

            val btnSecondary: TextView = dialog.findViewById(R.id.btn_secondary)
            btnSecondary.background = gradientDrawableButtonSecondary
            btnSecondary.setTextColor(
                Color.parseColor(
                    returnDefaultOrValidHexColor(
                        popupSecondaryButton.fontColor,
                        "#000000"
                    )
                )
            )
            btnSecondary.text = popupSecondaryButton.buttonText
            btnSecondary.setOnClickListener {

                inAppClickAction.onTrigger(
                    InAppConstants.Companion.EventClickType.SECONDARY_BUTTON
                )

                /*if (popupSecondaryButton.urlOnClick.isNotEmpty()){
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(popupSecondaryButton.urlOnClick))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    if (intent.resolveActivity(context.packageManager) != null)
                        context.startActivity(intent)
                    else {
//                        Toast.makeText(context, "Not able to handle the request.", Toast.LENGTH_LONG).show()
                        logger.debug("showDialog: Not able to handle the request.")
                    }
                } else {
//                    Toast.makeText(context, "Not able to handle the request.", Toast.LENGTH_LONG).show()
                    logger.debug("showDialog: Not able to handle the request.")
                }*/

                dialog.dismiss()
            }

            dialog.show()


            if (autoDismissInterval > 0) {
                CoroutineScope(Dispatchers.Default).launch {
                    val t = TimeUnit.SECONDS.toMillis(autoDismissInterval)
                    logger.info("autoDismissInterval: [$t milli sec or $autoDismissInterval sec].")
                    delay(t)
                    if (dialog.isShowing) {
                        withContext(Dispatchers.Main) {
                            dialog.dismiss()
                        }
                    }
                }
            }
        }

        internal fun showFullscreenDialog(
            context: Context,
            popUpBackgroundColor: String,
            popUpHeader: PopupHeader,
            popupMessage: PopupMessage,
            imageUrl: String,
            urlForOnClickOnImage: String,
            popupPrimaryButton: PopupPrimaryButton,
            popupSecondaryButton: PopupSecondaryButton,
            inAppClickAction: InAppClickAction
        ) {
            val dialog = Dialog(context, R.style.custom_style_dialog)

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.dialog_popup_fullscreen_triggered)

//            val frameLayout: FrameLayout = dialog.findViewById(R.id.frame_layout_root)
//            val gradientDrawable: GradientDrawable = frameLayout.background as GradientDrawable
//            gradientDrawable.setColor(Color.parseColor(returnDefaultOrValidHexColor(popUpBackgroundColor, "#FFFFFF")))

            val view: View = dialog.findViewById(R.id.view_close)
            view.setOnClickListener {

                inAppClickAction.onTrigger(
                    InAppConstants.Companion.EventClickType.CLOSE_EVENT
                )

                dialog.dismiss()
            }

            // TODO: close gitHub-> https://github.com/dheerajbhaskar/castled-notifications-android/issues/54
            val imageView: AppCompatImageView = dialog.findViewById(R.id.img_popup)
            Glide
                .with(context)
                .load(imageUrl)
//                .centerInside()
                .fitCenter()
//                .centerCrop()
                .into(imageView)
            imageView.setOnClickListener {
                if (urlForOnClickOnImage.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlForOnClickOnImage))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    if (intent.resolveActivity(context.packageManager) != null)
                        context.startActivity(intent)
                }

                inAppClickAction.onTrigger(
                    InAppConstants.Companion.EventClickType.IMAGE_CLICK
                )

                // TODO: close gitHub(on image top dialog will not close)-> https://github.com/dheerajbhaskar/castled-notifications-android/issues/54
                //below line commented because the Dialog not need to close when tapping on the image
//                dialog.dismiss()
            }

            val textHeader: TextView = dialog.findViewById(R.id.txt_header)
            textHeader.setBackgroundColor(
                Color.parseColor(
                    returnDefaultOrValidHexColor(
                        popUpHeader.backgroundColor,
                        "#FFFFFF"
                    )
                )
            )
            textHeader.setTextColor(
                Color.parseColor(
                    returnDefaultOrValidHexColor(
                        popUpHeader.fontColor,
                        "#000000"
                    )
                )
            )
            textHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, popUpHeader.fontSize)
            textHeader.text = popUpHeader.header

            val textMessage: TextView = dialog.findViewById(R.id.txt_message)
            textMessage.setBackgroundColor(
                Color.parseColor(
                    returnDefaultOrValidHexColor(
                        popupMessage.backgroundColor,
                        "#FFFFFF"
                    )
                )
            )
            textMessage.setTextColor(
                Color.parseColor(
                    returnDefaultOrValidHexColor(
                        popupMessage.fontColor,
                        "#000000"
                    )
                )
            )
            textMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, popupMessage.fontSize)
            textMessage.text = popupMessage.message

            val gradientDrawableButtonPrimary = GradientDrawable()
            gradientDrawableButtonPrimary.setColor(
                Color.parseColor(
                    returnDefaultOrValidHexColor(
                        popupPrimaryButton.buttonColor,
                        "#FFFFFF"
                    )
                )
            )
            gradientDrawableButtonPrimary.cornerRadius = 10f
            gradientDrawableButtonPrimary.setStroke(
                2,
                Color.parseColor(
                    returnDefaultOrValidHexColor(
                        popupPrimaryButton.borderColor,
                        "#FFFFFF"
                    )
                )
            )

            val btnPrimary: TextView = dialog.findViewById(R.id.btn_primary)
            btnPrimary.background = gradientDrawableButtonPrimary
            btnPrimary.setTextColor(
                Color.parseColor(
                    returnDefaultOrValidHexColor(
                        popupPrimaryButton.fontColor,
                        "#000000"
                    )
                )
            )
            btnPrimary.text = popupPrimaryButton.buttonText
            btnPrimary.setOnClickListener {
                inAppClickAction.onTrigger(
                    InAppConstants.Companion.EventClickType.PRIMARY_BUTTON
                )

                if (popupPrimaryButton.urlOnClick.isNotEmpty()) {
                    val intent =
                        Intent(Intent.ACTION_VIEW, Uri.parse(popupPrimaryButton.urlOnClick))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    if (intent.resolveActivity(context.packageManager) != null)
                        context.startActivity(intent)
                }

                dialog.dismiss()
            }

            val gradientDrawableButtonSecondary = GradientDrawable()
            gradientDrawableButtonSecondary.setColor(
                Color.parseColor(
                    returnDefaultOrValidHexColor(
                        popupSecondaryButton.buttonColor,
                        "#FFFFFF"
                    )
                )
            )
            gradientDrawableButtonSecondary.cornerRadius = 10f
            gradientDrawableButtonSecondary.setStroke(
                2,
                Color.parseColor(
                    returnDefaultOrValidHexColor(
                        popupSecondaryButton.borderColor,
                        "#FFFFFF"
                    )
                )
            )

            val btnSecondary: TextView = dialog.findViewById(R.id.btn_secondary)
            btnSecondary.background = gradientDrawableButtonSecondary
            btnSecondary.setTextColor(
                Color.parseColor(
                    returnDefaultOrValidHexColor(
                        popupSecondaryButton.fontColor,
                        "#000000"
                    )
                )
            )
            btnSecondary.text = popupSecondaryButton.buttonText
            btnSecondary.setOnClickListener {

                inAppClickAction.onTrigger(
                    InAppConstants.Companion.EventClickType.SECONDARY_BUTTON
                )
                if (popupSecondaryButton.urlOnClick.isNotEmpty()) {
                    val intent =
                        Intent(Intent.ACTION_VIEW, Uri.parse(popupSecondaryButton.urlOnClick))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    if (intent.resolveActivity(context.packageManager) != null)
                        context.startActivity(intent)
                }
                dialog.dismiss()
            }

            dialog.show()
        }

        internal fun showSlideUpDialog(
            context: Context,
            popUpBackgroundColor: String,
            popupMessage: PopupMessage,
            imageUrl: String,
            urlForOnClickOnImage: String,
            inAppClickAction: InAppClickAction
        ) {

            val dialog = Dialog(context, R.style.custom_style_dialog_bottom)

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.dialog_popup_bottom_triggered)

            val frameLayout: FrameLayout = dialog.findViewById(R.id.frame_layout_root)
            val gradientDrawable: GradientDrawable = frameLayout.background as GradientDrawable
            gradientDrawable.setColor(
                Color.parseColor(
                    returnDefaultOrValidHexColor(
                        popUpBackgroundColor,
                        "#FFFFFF"
                    )
                )
            )

            val view: View = dialog.findViewById(R.id.view_close)
            view.setOnClickListener {

                inAppClickAction.onTrigger(
                    InAppConstants.Companion.EventClickType.CLOSE_EVENT
                )

                dialog.dismiss()
            }

            val imageView: ImageView = dialog.findViewById(R.id.img_popup)
            Glide.with(context).load(imageUrl).into(imageView)
            imageView.setOnClickListener {
                inAppClickAction.onTrigger(
                    InAppConstants.Companion.EventClickType.IMAGE_CLICK
                )
                if (urlForOnClickOnImage.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlForOnClickOnImage))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    if (intent.resolveActivity(context.packageManager) != null)
                        context.startActivity(intent)
                }

                dialog.dismiss()
            }

            val textMessage: TextView = dialog.findViewById(R.id.txt_message)
            textMessage.setBackgroundColor(
                Color.parseColor(
                    returnDefaultOrValidHexColor(
                        popupMessage.backgroundColor,
                        "#FFFFFF"
                    )
                )
            )
            textMessage.setTextColor(
                Color.parseColor(
                    returnDefaultOrValidHexColor(
                        popupMessage.fontColor,
                        "#000000"
                    )
                )
            )
            textMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, popupMessage.fontSize)
            textMessage.text = popupMessage.message

            dialog.show()
        }

        private fun returnDefaultOrValidHexColor(
            hexColor: String,
            defaultHexColor: String
        ): String {
            if (hexColor.length == 7) {
                val colorPattern: Pattern = Pattern.compile("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")
                return if (colorPattern.matcher(hexColor).matches()) hexColor
                else if (colorPattern.matcher(defaultHexColor).matches()) defaultHexColor
                else throw Exception()
            } else return defaultHexColor
        }

        internal fun showFullscreenDialog(
            context: Context,
            activity: Activity,
            popUpBackgroundColor: String,
            popUpHeader: PopupHeader,
            popupMessage: PopupMessage,
            imageUrl: String,
            urlForOnClickOnImage: String,
            popupPrimaryButton: PopupPrimaryButton,
            popupSecondaryButton: PopupSecondaryButton
        ) {
            val displayRectangle = Rect()
            val window: Window = activity.window
            window.decorView.getWindowVisibleDisplayFrame(displayRectangle)

            val builder = AlertDialog.Builder(context, R.style.CustomAlertDialog)
            val viewGroup: ViewGroup = activity.findViewById(android.R.id.content)
            val dialogView: View =
                LayoutInflater.from(context)
                    .inflate(R.layout.dialog_popup_fullscreen_triggered, viewGroup, false)
            dialogView.minimumWidth = (displayRectangle.width() * 1f).toInt()
            dialogView.minimumHeight = (displayRectangle.height() * 1f).toInt()
            builder.setView(dialogView)
            val dialog = builder.create()

            val frameLayout: FrameLayout = dialogView.findViewById(R.id.frame_layout_root)
            val gradientDrawable: GradientDrawable = frameLayout.background as GradientDrawable
            gradientDrawable.setColor(
                Color.parseColor(
                    returnDefaultOrValidHexColor(
                        popUpBackgroundColor,
                        "#FFFFFF"
                    )
                )
            )

            val view: View = dialogView.findViewById(R.id.view_close)
            view.setOnClickListener { dialog.dismiss() }

            val imageView: ImageView = dialogView.findViewById(R.id.img_popup)
            Glide.with(context).load(imageUrl).into(imageView)
            imageView.setOnClickListener {
                if (urlForOnClickOnImage.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlForOnClickOnImage))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }

            }

            val textHeader: TextView = dialogView.findViewById(R.id.txt_header)
            textHeader.setBackgroundColor(
                Color.parseColor(
                    returnDefaultOrValidHexColor(
                        popUpHeader.backgroundColor,
                        "#FFFFFF"
                    )
                )
            )
            textHeader.setTextColor(
                Color.parseColor(
                    returnDefaultOrValidHexColor(
                        popUpHeader.fontColor,
                        "#000000"
                    )
                )
            )
            textHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, popUpHeader.fontSize)
            textHeader.text = popUpHeader.header

            val textMessage: TextView = dialogView.findViewById(R.id.txt_message)
            textMessage.setBackgroundColor(
                Color.parseColor(
                    returnDefaultOrValidHexColor(
                        popupMessage.backgroundColor,
                        "#FFFFFF"
                    )
                )
            )
            textMessage.setTextColor(
                Color.parseColor(
                    returnDefaultOrValidHexColor(
                        popupMessage.fontColor,
                        "#000000"
                    )
                )
            )
            textMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, popupMessage.fontSize)
            textMessage.text = popupMessage.message

            val gradientDrawableButtonPrimary = GradientDrawable()
            gradientDrawableButtonPrimary.setColor(
                Color.parseColor(
                    returnDefaultOrValidHexColor(
                        popupPrimaryButton.buttonColor,
                        "#FFFFFF"
                    )
                )
            )
            gradientDrawableButtonPrimary.cornerRadius = 10f
            gradientDrawableButtonPrimary.setStroke(
                2,
                Color.parseColor(
                    returnDefaultOrValidHexColor(
                        popupPrimaryButton.borderColor,
                        "#FFFFFF"
                    )
                )
            )

            val btnPrimary: TextView = dialogView.findViewById(R.id.btn_primary)
            btnPrimary.background = gradientDrawableButtonPrimary
            btnPrimary.setTextColor(
                Color.parseColor(
                    returnDefaultOrValidHexColor(
                        popupPrimaryButton.fontColor,
                        "#000000"
                    )
                )
            )
            btnPrimary.text = popupPrimaryButton.buttonText
            btnPrimary.setOnClickListener {
                dialog.dismiss()
                if (popupPrimaryButton.urlOnClick.isNotEmpty()) {
                    val intent =
                        Intent(Intent.ACTION_VIEW, Uri.parse(popupPrimaryButton.urlOnClick))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    if (intent.resolveActivity(context.packageManager) != null)
                        context.startActivity(intent)
                }
            }

            val gradientDrawableButtonSecondary = GradientDrawable()
            gradientDrawableButtonSecondary.setColor(
                Color.parseColor(
                    returnDefaultOrValidHexColor(
                        popupSecondaryButton.buttonColor,
                        "#FFFFFF"
                    )
                )
            )
            gradientDrawableButtonSecondary.cornerRadius = 10f
            gradientDrawableButtonSecondary.setStroke(
                2,
                Color.parseColor(
                    returnDefaultOrValidHexColor(
                        popupSecondaryButton.borderColor,
                        "#FFFFFF"
                    )
                )
            )

            val btnSecondary: TextView = dialogView.findViewById(R.id.btn_secondary)
            btnSecondary.background = gradientDrawableButtonSecondary
            btnSecondary.setTextColor(
                Color.parseColor(
                    returnDefaultOrValidHexColor(
                        popupSecondaryButton.fontColor,
                        "#000000"
                    )
                )
            )
            btnSecondary.text = popupSecondaryButton.buttonText
            btnSecondary.setOnClickListener {
                dialog.dismiss()
                if (popupSecondaryButton.urlOnClick.isNotEmpty()) {
                    val intent =
                        Intent(Intent.ACTION_VIEW, Uri.parse(popupSecondaryButton.urlOnClick))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    if (intent.resolveActivity(context.packageManager) != null)
                        context.startActivity(intent)
                }
            }

            dialog.show()
        }

        internal fun getTriggerEventType(notificationModel: Campaign?): InAppConstants.Companion.InAppTemplateType {
            if (notificationModel == null) return InAppConstants.Companion.InAppTemplateType.NONE

            val message: JsonObject = notificationModel.message.asJsonObject

            if (message.has("type")) {
                when (message.get("type").asString) {
                    "MODAL" -> return InAppConstants.Companion.InAppTemplateType.MODAL
                    "FULL_SCREEN" -> return InAppConstants.Companion.InAppTemplateType.FULL_SCREEN
                    "SLIDE_UP" -> return InAppConstants.Companion.InAppTemplateType.SLIDE_UP
                }
            }

            return InAppConstants.Companion.InAppTemplateType.NONE
        }
    }
}