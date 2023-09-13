package io.castled.android.notifications.inbox.views

import android.content.Context
import android.util.DisplayMetrics
import android.view.Display
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.RecyclerView
import io.castled.android.notifications.databinding.CastledInboxCellBinding

class CastledInboxAdapter(val context : Context, val list : List<String>) : RecyclerView.Adapter<CastledInboxAdapter.ViewHolder>() {
    // Inner ViewHolder class
    class ViewHolder(val binding : CastledInboxCellBinding) : RecyclerView.ViewHolder(binding.root){}

    // DAO instance to interact with the database

    private var screenWidth = getScreenWidth()
    // function to inflate the layout for each contact and create a new ViewHolder instance
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CastledInboxCellBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        )
    }

    // function to bind the data to the view elements of the ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.txtTitle.text = "Title"//list[position].name
        holder.binding.txtBody.text = "Body"//list[position].number
        // delete button onClickListener to delete the
        // contact from the database and notify the
        // adapter of the change
//        holder.binding.deleteButton.setOnClickListener{
//            dao.delete(list[position])
//            notifyItemRemoved(position)
//        }


        val width = screenWidth - 20
        val layoutParams =   holder.binding.imgCover.layoutParams
        layoutParams.height =  width
        holder.binding.imgCover.layoutParams = layoutParams//holder.binding.imgCover.layoutParams.width

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
        // itemView onClickListener to make a phone call
        // to the number of the contact
        holder.itemView.setOnClickListener{
//            val intent = Intent(Intent.ACTION_CALL, Uri.parse("" + list[position].number))
//            context.startActivity(intent)
        }
    }

    private fun getScreenWidth(): Int {
        val outMetrics = DisplayMetrics()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val display = context.display
            display?.getRealMetrics(outMetrics)
            if (display != null) {
                return display.width
            }

        } else {
            @Suppress("DEPRECATION")
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display: Display = windowManager.defaultDisplay
            @Suppress("DEPRECATION")
            display.getMetrics(outMetrics)
            return display.width
        }
        return 0
    }

    // function returns the number of items in the list
    override fun getItemCount(): Int {
        return  100
        return list.size
    }
}
