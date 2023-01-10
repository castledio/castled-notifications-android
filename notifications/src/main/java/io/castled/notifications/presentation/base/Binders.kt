package io.castled.notifications.presentation.base

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.button.MaterialButton

@BindingAdapter("android:visibility")
fun View.visibility(state: Boolean) {
    visibility = if (state) View.VISIBLE else View.GONE
}

@BindingAdapter("app:setTextColor")
fun MaterialButton.setTextColor(textColor: String) {
    try {
        if (textColor.isNotEmpty())
            setTextColor(Color.parseColor(textColor))
    } catch (ignored: Exception) { }
}

@BindingAdapter("app:setBackgroundColor")
fun MaterialButton.setBackgroundColor(backgroundColor: String) {
    try {
        if (backgroundColor.isNotEmpty())
            backgroundTintList = ColorStateList.valueOf(Color.parseColor(backgroundColor))
    } catch (ignored: Exception) { }
}

@BindingAdapter("app:setStrokeColor")
fun MaterialButton.setStrokeColor(strokeColor: String) {
    try {
        if (strokeColor.isNotEmpty())
            setStrokeColor(ColorStateList.valueOf(Color.parseColor(strokeColor)))
    } catch (ignored: Exception) { }
}

@BindingAdapter("showToast")
fun View.showToast(message: String?) {

    message?.let {

        setOnClickListener {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}

@BindingAdapter(
    value = ["imageUrl", "dontScale", "placeholder", "progressView", "errorIcon"],
    requireAll = false
)
fun ImageView.loadImageFromUrlOrPlaceholder(
    url: String?,
    dontScale: Boolean?,
    placeholder: Int?,
    progressBar: ProgressBar?,
    errorIcon: ImageView?
) {

    if (!url.isNullOrEmpty()) {

        val requestListener: RequestListener<Drawable> = object : RequestListener<Drawable> {

            override fun onLoadFailed(
                e: GlideException?,
                model: Any,
                target: Target<Drawable>,
                isFirstResource: Boolean
            ): Boolean {

                errorIcon?.let {

                    if (it.visibility == View.GONE)
                        it.visibility = View.VISIBLE
                }

                progressBar?.let {

                    if (it.visibility == View.VISIBLE)
                        it.visibility = View.GONE
                }

                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any,
                target: Target<Drawable>,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {

                errorIcon?.let {

                    if (it.visibility == View.VISIBLE)
                        it.visibility = View.GONE
                }

                progressBar?.let {

                    if (it.visibility == View.VISIBLE)
                        it.visibility = View.GONE
                }

                return false
            }
        }

        if (dontScale != null && dontScale) {

            Glide.with(context).load(url).listener(requestListener).into(this)
        } else {

            Glide.with(context).load(url).listener(requestListener).centerCrop().into(this)
        }
    } else {

        placeholder?.let {
            Glide.with(context)
                .load(placeholder).into(this)
        }
    }
}