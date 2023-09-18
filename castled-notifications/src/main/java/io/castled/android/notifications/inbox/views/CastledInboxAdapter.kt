package io.castled.android.notifications.inbox.views

import android.content.Context
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
import io.castled.android.notifications.databinding.CastledInboxCellBinding
import io.castled.android.notifications.inbox.model.InboxMessageType
import io.castled.android.notifications.store.models.AppInbox
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
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
        // delete button onClickListener to delete the
        // contact from the database and notify the
        // adapter of the change
//        holder.binding.deleteButton.setOnClickListener{
//            dao.delete(list[position])
//            notifyItemRemoved(position)
//        }


//        holder.binding.imgCover.post(object : java.lang.Runnable {
//            override fun run() {
//                val layoutParams =   holder.binding.imgCover.layoutParams
//
//                layoutParams.height =  holder.binding.imgCover.width
//                holder.binding.imgCover.layoutParams = layoutParams//holder.binding.imgCover.layoutParams.width
////                holder.binding.imgCover.visibility = View.GONE
//            }
//        })
//        holder.binding.imgCover.visibility = View.GONE
        holder.itemView.setOnClickListener {
//            val intent = Intent(Intent.ACTION_CALL, Uri.parse("" + list[position].number))
//            context.startActivity(intent)
        }
        populateButtons(holder, inbox)
    }

    private fun populateButtons(holder: ViewHolder, inbox: AppInbox) {
        val actionButtons = inbox.message["actionButtons"] as? JsonArray
        val buttonsLayout = holder.binding.buttonContainer
        actionButtons?.let {
            // Loop through each child view (buttons) in the LinearLayout
            for (i in 0 until buttonsLayout.childCount) {
                val child = buttonsLayout.getChildAt(i)
                if (child is TextView) {
                    // Determine if there's a corresponding button data in actionButtons
                    if (i < it.size) {
                        val buttonDetails = actionButtons[i] as? JsonObject
                        buttonDetails?.let {
                            child.text = buttonDetails["label"]?.jsonPrimitive?.content ?: ""
                        }
                        // Set button visibility based on button text (customize this logic)
                        child.visibility = if (child.text.isNotEmpty()) View.VISIBLE else View.GONE
                        // Set button click listener (customize this)
                        child.setOnClickListener {
                            // Handle button click action here
                            println("buttonDetailsbuttonDetails $buttonDetails")
                        }
                    } else {
                        // If there's no corresponding data in actionButtons, hide the button
                        child.visibility = View.GONE
                    }
                }
            }
            buttonsLayout.visibility = holder.binding.button1.visibility
        } ?: run {
            buttonsLayout.visibility = View.GONE
        }
    }


    private fun getScreenWidth(): Int {
        val outMetrics = DisplayMetrics()
        return if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val metrics: WindowMetrics = context.getSystemService(WindowManager::class.java).currentWindowMetrics
            metrics.bounds.width()

        } else {
            @Suppress("DEPRECATION") val windowManager =
                context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display: Display = windowManager.defaultDisplay
            @Suppress("DEPRECATION") display.getMetrics(outMetrics)
            display.width
        }
    }

    internal fun setInboxItems(inboxItemsList: List<AppInbox>) {
        this.inboxItemsList = ArrayList(inboxItemsList)
        notifyDataSetChanged()
    }

    // function returns the number of items in the list
    override fun getItemCount(): Int {
        return inboxItemsList.size
    }
}
