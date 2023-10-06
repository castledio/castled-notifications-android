package io.castled.android.notifications.inbox.views

import SwipeToDeleteCallback
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.castled.android.notifications.commons.ColorUtils
import io.castled.android.notifications.databinding.ActivityCastledInboxBinding
import io.castled.android.notifications.inbox.AppInboxHelper
import io.castled.android.notifications.inbox.InboxLifeCycleListenerImpl
import io.castled.android.notifications.inbox.model.CastledInboxConfig
import io.castled.android.notifications.inbox.model.InboxResponseConverter.toInboxItem
import io.castled.android.notifications.inbox.viewmodel.InboxRepository
import io.castled.android.notifications.store.models.AppInbox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.Serializable


class CastledInboxActivity : AppCompatActivity(),
    CoroutineScope by CoroutineScope(Dispatchers.Main) {

    private val inboxViewLifecycleListener = InboxLifeCycleListenerImpl(this)
    private lateinit var binding: ActivityCastledInboxBinding
    private val inboxRepository = InboxRepository(this)
    private lateinit var inboxListAdapter: CastledInboxAdapter
    val displayedItems = mutableSetOf<AppInbox>()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // inflate the layout
        binding = ActivityCastledInboxBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prepareRecyclerView()
        binding.imgClose.setOnClickListener { finishAfterTransition() }
        inboxRepository.observeInboxLiveData().observe(this) { inboxList ->
            inboxRepository.cachedInboxItems.clear()
            inboxRepository.cachedInboxItems.addAll(inboxList)
            inboxListAdapter.setInboxItems(inboxList)
            binding.inboxRecyclerView.visibility =
                if (inboxList.isEmpty()) View.GONE else View.VISIBLE
            binding.txtEmptyView.visibility =
                if (inboxList.isEmpty()) View.VISIBLE else View.GONE


        }
        launch(Dispatchers.Default) {
            inboxRepository.refreshInbox()
        }

        val displayConfig = getSerializable(
            this, "displayConfig",
            CastledInboxConfig::class.java
        )
        displayConfig?.let {
            customizeViews(displayConfig)
        } ?: run {
            supportActionBar?.let {
                binding.toolbar.visibility = View.GONE
            }
        }

    }

    private fun customizeViews(displayConfig: CastledInboxConfig) {

        binding.toolbar.setBackgroundColor(
            ColorUtils.parseColor(
                displayConfig.navigationBarBackgroundColor,
                Color.WHITE
            )
        )
        binding.txtEmptyView.text = displayConfig.emptyMessageViewText
        binding.txtEmptyView.setTextColor(
            ColorUtils.parseColor(
                displayConfig.navigationBarBackgroundColor,
                Color.BLACK
            )
        )
        binding.toolbarTitle.text = displayConfig.navigationBarTitle
        binding.toolbarTitle.setTextColor(
            ColorUtils.parseColor(
                displayConfig.navigationBarTitleColor,
                android.R.attr.colorPrimary
            )
        )
        binding.viewBg.setBackgroundColor(
            ColorUtils.parseColor(
                displayConfig.inboxViewBackgroundColor,
                Color.WHITE
            )
        )
        binding.toolbar.visibility =
            if (displayConfig.hideNavigationBar) View.GONE else View.VISIBLE

        val actionBar = supportActionBar
        actionBar?.let {
            if (displayConfig.hideNavigationBar) {
                actionBar.hide()
            } else {
                actionBar.setBackgroundDrawable(
                    ColorDrawable(
                        ColorUtils.parseColor(
                            displayConfig.navigationBarBackgroundColor,
                            (android.R.attr.colorPrimary)
                        )
                    )
                );
                val text: Spannable = SpannableString(displayConfig.navigationBarTitle)
                text.setSpan(
                    ForegroundColorSpan(
                        ColorUtils.parseColor(
                            displayConfig.navigationBarTitleColor,
                            Color.WHITE
                        )
                    ),
                    0,
                    text.length,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
                actionBar.title = text
            }
            binding.toolbar.visibility = View.GONE
        }
    }

    override fun onPause() {
        super.onPause()
        if (displayedItems.size > 0) {
            inboxViewLifecycleListener.registerReadEvents(displayedItems)
        }
        // Perform actions when the fragment is no longer visible
    }

    private fun <T : Serializable?> getSerializable(
        activity: Activity,
        name: String,
        clazz: Class<T>
    ): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            activity.intent.getSerializableExtra(name, clazz)
        else
            activity.intent.getSerializableExtra(name) as? T
    }

    private fun prepareRecyclerView() {
        inboxListAdapter = CastledInboxAdapter(this)
        binding.inboxRecyclerView.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = inboxListAdapter
            val itemTouchHelper =
                ItemTouchHelper(SwipeToDeleteCallback(adapter as CastledInboxAdapter))
            itemTouchHelper.attachToRecyclerView(binding.inboxRecyclerView)
            binding.inboxRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val firstVisibleItemPosition =
                        (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    val lastVisibleItemPosition =
                        (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                    for (i in firstVisibleItemPosition..lastVisibleItemPosition) {
                        if (i >= 0 && i < inboxListAdapter.inboxItemsList.size) {
                            val data = inboxListAdapter.inboxItemsList[i]
                            if (!data.isRead) {
                                displayedItems.add(data)
                            }
                        }
                    }

                }
            })
        }
    }

    internal fun deleteItem(position: Int, item: AppInbox) {
        binding.progressBar.visibility = ProgressBar.VISIBLE
        AppInboxHelper.deleteInboxItem(item.toInboxItem()) { success, message ->
            this@CastledInboxActivity.launch(Dispatchers.Main) {
                if (success) {
                    displayedItems.remove(item)
                } else {
                    val toast = Toast.makeText(
                        this@CastledInboxActivity, message, Toast.LENGTH_LONG
                    )
                    toast.show()
                    inboxListAdapter.inboxItemsList.add(position, item)
                    inboxListAdapter.reloadRecyclerView()
                }
                binding.progressBar.visibility = ProgressBar.GONE
            }
        }
    }

    internal fun onClicked(
        inboxItem: AppInbox, actionParams: Map<String, Any>
    ) {
        inboxViewLifecycleListener.onClicked(inboxItem, actionParams)
    }
}
