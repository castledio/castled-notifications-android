package io.castled.inappNotifications.trigger

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
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import io.castled.inappNotifications.models.NotificationModel
import io.castled.inappNotifications.notificationConsts.NotificationConstants
import io.castled.notifications.R
import java.util.regex.Pattern

private const val TAG = "TriggerPopup"
class TriggerPopupDialog {

    companion object {

        internal fun showDialog(
            context: Context,
            popUpBackgroundColor: String,
            popUpHeader: PopupHeader,
            popupMessage: PopupMessage,
            imageUrl:String,
            urlForOnClickOnImage: String,
            popupPrimaryButton: PopupPrimaryButton,
            popupSecondaryButton: PopupSecondaryButton
        ) {
            val dialog = Dialog(context)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.dialog_popup_triggered)

            val frameLayout: FrameLayout = dialog.findViewById(R.id.frame_layout_root)
            val gradientDrawable: GradientDrawable = frameLayout.background as GradientDrawable
            gradientDrawable.setColor(Color.parseColor(returnDefaultOrValidHexColor(popUpBackgroundColor, "#FFFFFF")))

            val view: View = dialog.findViewById(R.id.view_close)
            view.setOnClickListener { dialog.dismiss() }

            val imageView: ImageView = dialog.findViewById(R.id.img_popup)
            Glide.with(context).load(imageUrl).into(imageView)
            imageView.setOnClickListener {
                if (urlForOnClickOnImage.isNotEmpty()){
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlForOnClickOnImage))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }

            }

            val textHeader: TextView = dialog.findViewById(R.id.txt_header)
            textHeader.setBackgroundColor(Color.parseColor(returnDefaultOrValidHexColor(popUpHeader.backgroundColor, "#FFFFFF")))
            textHeader.setTextColor(Color.parseColor(returnDefaultOrValidHexColor(popUpHeader.fontColor, "#000000")))
            textHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP,popUpHeader.fontSize)
            textHeader.text = popUpHeader.header

            val textMessage: TextView = dialog.findViewById(R.id.txt_message)
            textMessage.setBackgroundColor(Color.parseColor(returnDefaultOrValidHexColor(popupMessage.backgroundColor, "#FFFFFF")))
            textMessage.setTextColor(Color.parseColor(returnDefaultOrValidHexColor(popupMessage.fontColor, "#000000")))
            textMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP,popupMessage.fontSize)
            textMessage.text = popupMessage.message

            val gradientDrawableButtonPrimary = GradientDrawable()
            gradientDrawableButtonPrimary.setColor(Color.parseColor(returnDefaultOrValidHexColor(popupPrimaryButton.buttonColor, "#FFFFFF")))
            gradientDrawableButtonPrimary.cornerRadius = 10f
            gradientDrawableButtonPrimary.setStroke(2, Color.parseColor(returnDefaultOrValidHexColor(popupPrimaryButton.borderColor, "#FFFFFF")))

            val btnPrimary: TextView = dialog.findViewById(R.id.btn_primary)
            btnPrimary.background = gradientDrawableButtonPrimary
            btnPrimary.setTextColor(Color.parseColor(returnDefaultOrValidHexColor(popupPrimaryButton.fontColor, "#000000")))
            btnPrimary.text = popupPrimaryButton.buttonText
            btnPrimary.setOnClickListener {
                dialog.dismiss()
                if (popupPrimaryButton.urlOnClick.isNotEmpty()){
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(popupPrimaryButton.urlOnClick))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    if (intent.resolveActivity(context.packageManager) != null)
                        context.startActivity(intent)
                    else Toast.makeText(context, "Not able to handle the request.", Toast.LENGTH_LONG).show()
                } else Toast.makeText(context, "Not able to handle the request.", Toast.LENGTH_LONG).show()
            }

            val gradientDrawableButtonSecondary = GradientDrawable()
            gradientDrawableButtonSecondary.setColor(Color.parseColor(returnDefaultOrValidHexColor(popupSecondaryButton.buttonColor, "#FFFFFF")))
            gradientDrawableButtonSecondary.cornerRadius = 10f
            gradientDrawableButtonSecondary.setStroke(2, Color.parseColor(returnDefaultOrValidHexColor(popupSecondaryButton.borderColor, "#FFFFFF")))

            val btnSecondary: TextView = dialog.findViewById(R.id.btn_secondary)
            btnSecondary.background = gradientDrawableButtonSecondary
            btnSecondary.setTextColor(Color.parseColor(returnDefaultOrValidHexColor(popupSecondaryButton.fontColor, "#000000")))
            btnSecondary.text = popupSecondaryButton.buttonText
            btnSecondary.setOnClickListener {
                dialog.dismiss()
                if (popupSecondaryButton.urlOnClick.isNotEmpty()){
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(popupSecondaryButton.urlOnClick))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    if (intent.resolveActivity(context.packageManager) != null)
                        context.startActivity(intent)
                    else Toast.makeText(context, "Not able to handle the request.", Toast.LENGTH_LONG).show()
                } else Toast.makeText(context, "Not able to handle the request.", Toast.LENGTH_LONG).show()
            }

            dialog.show()
        }

        internal fun showFullscreenDialog(
            context: Context,
            popUpBackgroundColor: String,
            popUpHeader: PopupHeader,
            popupMessage: PopupMessage,
            imageUrl:String,
            urlForOnClickOnImage: String,
            popupPrimaryButton: PopupPrimaryButton,
            popupSecondaryButton: PopupSecondaryButton
        ) {
            val dialog = Dialog(context, R.style.custom_style_dialog)

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.dialog_popup_fullscreen_triggered)

            val frameLayout: FrameLayout = dialog.findViewById(R.id.frame_layout_root)
            val gradientDrawable: GradientDrawable = frameLayout.background as GradientDrawable
            gradientDrawable.setColor(Color.parseColor(returnDefaultOrValidHexColor(popUpBackgroundColor, "#FFFFFF")))

            val view: View = dialog.findViewById(R.id.view_close)
            view.setOnClickListener { dialog.dismiss() }

            val imageView: ImageView = dialog.findViewById(R.id.img_popup)
            Glide.with(context).load(imageUrl).into(imageView)
            imageView.setOnClickListener {
                if (urlForOnClickOnImage.isNotEmpty()){
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlForOnClickOnImage))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }

            }

            val textHeader: TextView = dialog.findViewById(R.id.txt_header)
            textHeader.setBackgroundColor(Color.parseColor(returnDefaultOrValidHexColor(popUpHeader.backgroundColor, "#FFFFFF")))
            textHeader.setTextColor(Color.parseColor(returnDefaultOrValidHexColor(popUpHeader.fontColor, "#000000")))
            textHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP,popUpHeader.fontSize)
            textHeader.text = popUpHeader.header

            val textMessage: TextView = dialog.findViewById(R.id.txt_message)
            textMessage.setBackgroundColor(Color.parseColor(returnDefaultOrValidHexColor(popupMessage.backgroundColor, "#FFFFFF")))
            textMessage.setTextColor(Color.parseColor(returnDefaultOrValidHexColor(popupMessage.fontColor, "#000000")))
            textMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP,popupMessage.fontSize)
            textMessage.text = popupMessage.message

            val gradientDrawableButtonPrimary = GradientDrawable()
            gradientDrawableButtonPrimary.setColor(Color.parseColor(returnDefaultOrValidHexColor(popupPrimaryButton.buttonColor, "#FFFFFF")))
            gradientDrawableButtonPrimary.cornerRadius = 10f
            gradientDrawableButtonPrimary.setStroke(2, Color.parseColor(returnDefaultOrValidHexColor(popupPrimaryButton.borderColor, "#FFFFFF")))

            val btnPrimary: TextView = dialog.findViewById(R.id.btn_primary)
            btnPrimary.background = gradientDrawableButtonPrimary
            btnPrimary.setTextColor(Color.parseColor(returnDefaultOrValidHexColor(popupPrimaryButton.fontColor, "#000000")))
            btnPrimary.text = popupPrimaryButton.buttonText
            btnPrimary.setOnClickListener {
                dialog.dismiss()
                if (popupPrimaryButton.urlOnClick.isNotEmpty()){
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(popupPrimaryButton.urlOnClick))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                if (intent.resolveActivity(context.packageManager) != null)
                    context.startActivity(intent)
                }
            }

            val gradientDrawableButtonSecondary = GradientDrawable()
            gradientDrawableButtonSecondary.setColor(Color.parseColor(returnDefaultOrValidHexColor(popupSecondaryButton.buttonColor, "#FFFFFF")))
            gradientDrawableButtonSecondary.cornerRadius = 10f
            gradientDrawableButtonSecondary.setStroke(2, Color.parseColor(returnDefaultOrValidHexColor(popupSecondaryButton.borderColor, "#FFFFFF")))

            val btnSecondary: TextView = dialog.findViewById(R.id.btn_secondary)
            btnSecondary.background = gradientDrawableButtonSecondary
            btnSecondary.setTextColor(Color.parseColor(returnDefaultOrValidHexColor(popupSecondaryButton.fontColor, "#000000")))
            btnSecondary.text = popupSecondaryButton.buttonText
            btnSecondary.setOnClickListener {
                dialog.dismiss()
                if (popupSecondaryButton.urlOnClick.isNotEmpty()){
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(popupSecondaryButton.urlOnClick))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                if (intent.resolveActivity(context.packageManager) != null)
                    context.startActivity(intent)
                }
            }

            dialog.show()
        }

        internal fun showSlideUpDialog(
            context: Context,
            popUpBackgroundColor: String,
            popupMessage: PopupMessage,
            imageUrl:String,
            urlForOnClickOnImage: String
        ) {

            val dialog = Dialog(context, R.style.custom_style_dialog_bottom)

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.dialog_popup_bottom_triggered)

            val frameLayout: FrameLayout = dialog.findViewById(R.id.frame_layout_root)
            val gradientDrawable: GradientDrawable = frameLayout.background as GradientDrawable
            gradientDrawable.setColor(Color.parseColor(returnDefaultOrValidHexColor(popUpBackgroundColor, "#FFFFFF")))

            val view: View = dialog.findViewById(R.id.view_close)
            view.setOnClickListener { dialog.dismiss() }

            val imageView: ImageView = dialog.findViewById(R.id.img_popup)
            Glide.with(context).load(imageUrl).into(imageView)
            imageView.setOnClickListener {
                if (urlForOnClickOnImage.isNotEmpty()){
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlForOnClickOnImage))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }

            }

            val textMessage: TextView = dialog.findViewById(R.id.txt_message)
//            textMessage.setBackgroundColor(Color.parseColor(returnDefaultOrValidHexColor(popupMessage.backgroundColor, "#FFFFFF")))
            textMessage.setTextColor(Color.parseColor(returnDefaultOrValidHexColor(popupMessage.fontColor, "#000000")))
            textMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP,popupMessage.fontSize)
            textMessage.text = popupMessage.message

            dialog.show()
        }

        private fun returnDefaultOrValidHexColor(hexColor: String , defaultHexColor: String): String {
//            val colorPattern: Pattern = Pattern.compile("#([0-9a-f]{3}|[0-9a-f]{6}|[0-9a-f]{8})")
            val colorPattern: Pattern = Pattern.compile("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")
            return if (colorPattern.matcher(hexColor).matches()) hexColor
            else if (colorPattern.matcher(defaultHexColor).matches()) defaultHexColor
            else throw Exception()
        }

        /*fun showFullScreenDialog(context: Context, activity: Activity){
            val displayRectangle = Rect()
            val window: Window = activity.window
            window.decorView.getWindowVisibleDisplayFrame(displayRectangle)
            val builder = AlertDialog.Builder(context, R.style.CustomAlertDialog)
            val viewGroup: ViewGroup = activity.findViewById(android.R.id.content)
            val dialogView: View =
                LayoutInflater.from(context).inflate(R.layout.dialog_popup_fullscreen_triggered, viewGroup, false)
            dialogView.minimumWidth = (displayRectangle.width() * 1f).toInt()
            dialogView.minimumHeight = (displayRectangle.height() * 1f).toInt()
            builder.setView(dialogView)
            val alertDialog = builder.create()
            val buttonOk = dialogView.findViewById<Button>(R.id.buttonOk)
            buttonOk.setOnClickListener { alertDialog.dismiss() }
            alertDialog.show()
        }*/

        internal fun showFullscreenDialog(
            context: Context,
            activity: Activity,
            popUpBackgroundColor: String,
            popUpHeader: PopupHeader,
            popupMessage: PopupMessage,
            imageUrl:String,
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
                LayoutInflater.from(context).inflate(R.layout.dialog_popup_fullscreen_triggered, viewGroup, false)
            dialogView.minimumWidth = (displayRectangle.width() * 1f).toInt()
            dialogView.minimumHeight = (displayRectangle.height() * 1f).toInt()
            builder.setView(dialogView)
            val dialog = builder.create()

//            val dialog = Dialog(context)
//            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//            dialog.setCancelable(false)
//            dialog.setContentView(R.layout.dialog_popup_triggered)

            val frameLayout: FrameLayout = dialogView.findViewById(R.id.frame_layout_root)
            val gradientDrawable: GradientDrawable = frameLayout.background as GradientDrawable
            gradientDrawable.setColor(Color.parseColor(returnDefaultOrValidHexColor(popUpBackgroundColor, "#FFFFFF")))

            val view: View = dialogView.findViewById(R.id.view_close)
            view.setOnClickListener { dialog.dismiss() }

            val imageView: ImageView = dialogView.findViewById(R.id.img_popup)
            Glide.with(context).load(imageUrl).into(imageView)
            imageView.setOnClickListener {
                if (urlForOnClickOnImage.isNotEmpty()){
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlForOnClickOnImage))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }

            }

            val textHeader: TextView = dialogView.findViewById(R.id.txt_header)
            textHeader.setBackgroundColor(Color.parseColor(returnDefaultOrValidHexColor(popUpHeader.backgroundColor, "#FFFFFF")))
            textHeader.setTextColor(Color.parseColor(returnDefaultOrValidHexColor(popUpHeader.fontColor, "#000000")))
            textHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP,popUpHeader.fontSize)
            textHeader.text = popUpHeader.header

            val textMessage: TextView = dialogView.findViewById(R.id.txt_message)
            textMessage.setBackgroundColor(Color.parseColor(returnDefaultOrValidHexColor(popupMessage.backgroundColor, "#FFFFFF")))
            textMessage.setTextColor(Color.parseColor(returnDefaultOrValidHexColor(popupMessage.fontColor, "#000000")))
            textMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP,popupMessage.fontSize)
            textMessage.text = popupMessage.message

            val gradientDrawableButtonPrimary = GradientDrawable()
            gradientDrawableButtonPrimary.setColor(Color.parseColor(returnDefaultOrValidHexColor(popupPrimaryButton.buttonColor, "#FFFFFF")))
            gradientDrawableButtonPrimary.cornerRadius = 10f
            gradientDrawableButtonPrimary.setStroke(2, Color.parseColor(returnDefaultOrValidHexColor(popupPrimaryButton.borderColor, "#FFFFFF")))

            val btnPrimary: TextView = dialogView.findViewById(R.id.btn_primary)
            btnPrimary.background = gradientDrawableButtonPrimary
            btnPrimary.setTextColor(Color.parseColor(returnDefaultOrValidHexColor(popupPrimaryButton.fontColor, "#000000")))
            btnPrimary.text = popupPrimaryButton.buttonText
            btnPrimary.setOnClickListener {
                dialog.dismiss()
                if (popupPrimaryButton.urlOnClick.isNotEmpty()){
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(popupPrimaryButton.urlOnClick))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                if (intent.resolveActivity(context.packageManager) != null)
                    context.startActivity(intent)
                }
            }

            val gradientDrawableButtonSecondary = GradientDrawable()
            gradientDrawableButtonSecondary.setColor(Color.parseColor(returnDefaultOrValidHexColor(popupSecondaryButton.buttonColor, "#FFFFFF")))
            gradientDrawableButtonSecondary.cornerRadius = 10f
            gradientDrawableButtonSecondary.setStroke(2, Color.parseColor(returnDefaultOrValidHexColor(popupSecondaryButton.borderColor, "#FFFFFF")))

            val btnSecondary: TextView = dialogView.findViewById(R.id.btn_secondary)
            btnSecondary.background = gradientDrawableButtonSecondary
            btnSecondary.setTextColor(Color.parseColor(returnDefaultOrValidHexColor(popupSecondaryButton.fontColor, "#000000")))
            btnSecondary.text = popupSecondaryButton.buttonText
            btnSecondary.setOnClickListener {
                dialog.dismiss()
                if (popupSecondaryButton.urlOnClick.isNotEmpty()){
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(popupSecondaryButton.urlOnClick))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                if (intent.resolveActivity(context.packageManager) != null)
                    context.startActivity(intent)
                }
            }

            dialog.show()
        }

        internal fun getTriggerNotificationType(notificationModel: NotificationModel): NotificationConstants.Companion.NotificationType{
            val message:  JsonObject = notificationModel.message.asJsonObject

            if (message.has("type")){
                when(message.get("type").asString){
                    "MODAL" -> return NotificationConstants.Companion.NotificationType.MODAL
                    "FULL_SCREEN" -> return NotificationConstants.Companion.NotificationType.FULL_SCREEN
                    "SLIDE_UP" -> return NotificationConstants.Companion.NotificationType.SLIDE_UP
                }
            }

            return NotificationConstants.Companion.NotificationType.NONE
        }
    }
}