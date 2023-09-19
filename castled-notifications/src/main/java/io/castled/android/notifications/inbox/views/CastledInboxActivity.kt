package io.castled.android.notifications.inbox.views

import SwipeToDeleteCallback
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.castled.android.notifications.databinding.ActivityCastledInboxBinding
import io.castled.android.notifications.inbox.InboxLifeCycleListenerImpl
import io.castled.android.notifications.inbox.viewmodel.InboxRepository
import io.castled.android.notifications.store.models.AppInbox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CastledInboxActivity : AppCompatActivity(),
    CoroutineScope by CoroutineScope(Dispatchers.Main) {

    private val iboxViewLifecycleListener = InboxLifeCycleListenerImpl(this)
    private lateinit var binding: ActivityCastledInboxBinding
    private val inboxRepository = InboxRepository(this)
    private lateinit var inboxListAdapter: CastledInboxAdapter
    val displayedItems = mutableSetOf<AppInbox>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // inflate the layout
        binding = ActivityCastledInboxBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prepareRecyclerView()
        binding.imgClose.setOnClickListener { finishAfterTransition() }
        inboxRepository.observeMovieLiveData().observe(this, Observer { inboxList ->
            inboxRepository.cachedInboxItems.clear()
            inboxRepository.cachedInboxItems.addAll(inboxList)
            inboxListAdapter.setInboxItems(inboxList)
        })
        launch(Dispatchers.IO) {
            inboxRepository.refreshInbox()
        }
//        supportActionBar.let {
//            binding.toolbar.visibility = View.GONE
//        }

    }

    override fun onPause() {
        super.onPause()
        println(displayedItems)
        // Perform actions when the fragment is no longer visible
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
                            displayedItems.add(data)
                        }
                    }

                }
            })
        }
    }

    internal fun deleteItem(item: AppInbox) {
        displayedItems.remove(item)
    }

    internal fun onClicked(
        inboxItem: AppInbox, actionParams: Map<String, Any>
    ) {
        iboxViewLifecycleListener.onClicked(inboxItem, actionParams)
    }
}
