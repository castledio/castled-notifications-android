package io.castled.android.notifications.inbox.views

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.DisplayMetrics
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.WindowMetrics
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.castled.android.notifications.R
import io.castled.android.notifications.commons.DateTimeUtils
import io.castled.android.notifications.databinding.CastledInboxCellBinding
import io.castled.android.notifications.inbox.model.InboxMessageType
import io.castled.android.notifications.store.models.AppInbox
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive


class CastledInboxAdapter(val context: Context) :
    RecyclerView.Adapter<CastledInboxAdapter.ViewHolder>() {
    // DAO instance to interact with the database
    internal var inboxItemsList = ArrayList<AppInbox>()
    private var screenWidth = getScreenWidth()

    // Inner ViewHolder class
    class ViewHolder(val binding: CastledInboxCellBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    // function to inflate the layout for each contact and create a new ViewHolder instance
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CastledInboxCellBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    // function to bind the data to the view elements of the ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val inbox = inboxItemsList[position]
        holder.binding.txtTitle.text = inbox.title
        holder.binding.txtBody.text = inbox.body
        holder.binding.txtDate.text = DateTimeUtils.timeAgo(inbox.dateAdded)
        holder.binding.imgUnread.visibility = if (inbox.isRead) View.GONE else View.VISIBLE
        when (inbox.messageType) {
            InboxMessageType.MESSAGE_WITH_MEDIA -> {
                holder.binding.imgCover.visibility = View.VISIBLE
                holder.binding.cardLogoParent.visibility = View.GONE

                val width = screenWidth - 20
                val layoutParams = holder.binding.imgCover.layoutParams
                layoutParams.height = (width.toFloat() * inbox.aspectRatio.toFloat()).toInt()
                holder.binding.imgCover.layoutParams =
                    layoutParams//holder.binding.imgCover.layoutParams.width

                val placeholderImage = R.drawable.castled_placeholder
                Glide.with(holder.itemView).load(inbox.thumbnailUrl).placeholder(placeholderImage)
                    .into(holder.binding.imgCover)

            }

            InboxMessageType.MESSAGE_BANNER -> {
                holder.binding.imgCover.visibility = View.GONE
                holder.binding.cardLogoParent.visibility = View.VISIBLE

                val placeholderImage = R.drawable.castled_placeholder
                Glide.with(holder.itemView).load(inbox.thumbnailUrl).placeholder(placeholderImage)
                    .into(holder.binding.imgLogo)

            }

            InboxMessageType.MESSAGE_BANNER_NO_ICON -> {
                holder.binding.imgCover.visibility = View.GONE
                holder.binding.cardLogoParent.visibility = View.GONE

            }

            else -> {
                holder.binding.imgCover.visibility = View.GONE
                holder.binding.cardLogoParent.visibility = View.GONE
            }
        }
        holder.itemView.setOnClickListener {
            val defaultClickAction =
                inbox.message["defaultClickAction"]?.jsonPrimitive?.content ?: ""
            if (defaultClickAction.isNotEmpty()) {
                val url = inbox.message["url"]?.jsonPrimitive?.content ?: ""
                val map = mutableMapOf<String, Any>()
                map["clickAction"] = defaultClickAction
                map["url"] = url
                val keyVals =
                    inbox.message["keyVals"]?.jsonObject?.entries?.associate { (key, value) ->
                        key to value
                    }
                keyVals?.let {
                    map["keyVals"] = JsonObject(keyVals)
                }
                (context as CastledInboxActivity).onClicked(
                    inbox, map
                )

            }

        }
        populateLinks(holder, inbox)
        customizeViews(holder, inbox)
    }

    private fun getScreenWidth(): Int {
        val outMetrics = DisplayMetrics()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics: WindowMetrics =
                context.getSystemService(WindowManager::class.java).currentWindowMetrics
            metrics.bounds.width()

        } else {
            @Suppress("DEPRECATION") val windowManager =
                context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display: Display = windowManager.defaultDisplay
            @Suppress("DEPRECATION") display.getMetrics(outMetrics)
            display.width
        }
    }

    internal fun setInboxItems(inboxItems: List<AppInbox>) {
        inboxItemsList.clear()
        inboxItemsList.addAll(inboxItems)
        reloadRecyclerView()
    }

    internal fun reloadRecyclerView() {
        notifyDataSetChanged()
    }

    // function returns the number of items in the list
    override fun getItemCount(): Int {
        return inboxItemsList.size
    }

    internal fun deleteItem(position: Int) {
        try {
            val itemToDelete = inboxItemsList.get(position)
            inboxItemsList.removeAt(position)
            notifyItemRemoved(position)
            (context as CastledInboxActivity).deleteItem(position, itemToDelete)
        } catch (e: Exception) {

        }
    }

    private fun populateLinks(holder: ViewHolder, inbox: AppInbox) {
        val actionButtons = inbox.message["actionButtons"] as? JsonArray
        val linksLayout = holder.binding.linkContainer
        actionButtons?.let {
            // Loop through each child view (buttons) in the LinearLayout
            for (i in 0 until linksLayout.childCount) {
                val child = linksLayout.getChildAt(i)
                if (child is TextView) {
                    // Determine if there's a corresponding button data in actionButtons
                    if (i < it.size) {
                        val buttonDetails = actionButtons[i] as? JsonObject
                        buttonDetails?.let {
                            child.text = buttonDetails["label"]?.jsonPrimitive?.content ?: ""
                        }
                        child.setTextColor(
                            parseColor(
                                buttonDetails?.get("fontColor")?.jsonPrimitive?.content ?: "",
                                Color.BLUE
                            )
                        )
                        child.setBackgroundColor(
                            parseColor(
                                buttonDetails?.get("buttonColor")?.jsonPrimitive?.content ?: "",
                                Color.TRANSPARENT
                            )
                        )
                        // Set button visibility based on button text (customize this logic)
                        child.visibility = if (child.text.isNotEmpty()) View.VISIBLE else View.GONE
                        // Set button click listener (customize this)
                        child.setOnClickListener {
                            // Handle button click action here
                            buttonDetails?.let {
                                (context as CastledInboxActivity).onClicked(
                                    inbox, buttonDetails.toMap()
                                )
                            }
                        }
                    } else {
                        // If there's no corresponding data in actionButtons, hide the button
                        child.visibility = View.GONE
                    }
                }
            }
            linksLayout.visibility = holder.binding.link1.visibility
        } ?: run {
            linksLayout.visibility = View.GONE
        }
    }

    private fun customizeViews(holder: ViewHolder, inbox: AppInbox) {
        holder.binding.cardContainer.setCardBackgroundColor(
            parseColor(
                inbox.message["bgColor"]?.jsonPrimitive?.content ?: "", Color.WHITE
            )
        )
        holder.binding.txtTitle.setTextColor(
            parseColor(
                inbox.message["titleFontColor"]?.jsonPrimitive?.content ?: "", Color.BLACK
            )
        )
        holder.binding.txtBody.setTextColor(
            parseColor(
                inbox.message["bodyFontColor"]?.jsonPrimitive?.content ?: "", Color.BLACK
            )
        )
        holder.binding.txtDate.setTextColor(holder.binding.txtBody.currentTextColor)
    }

    private fun parseColor(colorStr: String, defaultColor: Int): Int {
        return try {
            Color.parseColor(colorStr)
        } catch (e: IllegalArgumentException) {
            defaultColor
        }
    }
}
