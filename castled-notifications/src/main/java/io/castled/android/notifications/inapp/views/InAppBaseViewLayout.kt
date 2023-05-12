package io.castled.android.notifications.inapp.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.*
import kotlinx.serialization.json.JsonObject

abstract class InAppBaseViewLayout(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

    abstract val viewContainer: View?
    abstract val headerView: TextView?
    abstract val messageView: TextView?
    abstract val imageView: ImageView?
    abstract val buttonViewContainer: View?
    abstract val primaryButton: Button?
    abstract val secondaryButton: Button?
    abstract val closeButton: ImageButton?

    abstract fun updateViewParams(message: JsonObject)
}