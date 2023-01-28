package io.castled.inappNotifications.modal

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.util.TypedValue
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import io.castled.notifications.R
import java.util.regex.Matcher
import java.util.regex.Pattern


class TriggerPopup {

    companion object {

        fun showDialog(
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

            val frameLayout: FrameLayout = dialog.findViewById(R.id.root)
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
            gradientDrawableButtonPrimary.cornerRadius = 10f;
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
            gradientDrawableButtonSecondary.cornerRadius = 10f;
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

        private fun returnDefaultOrValidHexColor(hexColor: String , defaultHexColor: String): String {
//            val colorPattern: Pattern = Pattern.compile("#([0-9a-f]{3}|[0-9a-f]{6}|[0-9a-f]{8})")
            val colorPattern: Pattern = Pattern.compile("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")
            val m: Matcher = colorPattern.matcher(hexColor)
            if (colorPattern.matcher(hexColor).matches()) return hexColor
            else if (colorPattern.matcher(defaultHexColor).matches()) return defaultHexColor
            else throw Exception()
        }
    }
}