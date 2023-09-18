package io.castled.android.notifications.inbox.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.castled.android.notifications.databinding.ActivityCastledInboxBinding
import io.castled.android.notifications.inbox.viewmodel.InboxRepository
import io.castled.android.notifications.store.models.AppInbox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CastledInboxActivity : AppCompatActivity(),
    CoroutineScope by CoroutineScope(Dispatchers.Main) {
    // private variable to inflate the layout for the activity
    private lateinit var binding: ActivityCastledInboxBinding
    private val inboxRepository = InboxRepository(this)
    private lateinit var inboxListAdapter: CastledInboxAdapter
    val displayedItems = mutableSetOf<AppInbox>()

    // variable to access the ViewModel class
    //  val viewModel : ContactViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // inflate the layout
        binding = ActivityCastledInboxBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prepareRecyclerView()
        binding.imgClose.setOnClickListener { finishAfterTransition() }
        prepareRecyclerView()
        launch(Dispatchers.IO) {
            try {
                inboxRepository.refreshInbox()
            } catch (e: Exception) {
            }
        }
        inboxRepository.observeMovieLiveData().observe(this, Observer { inboxList ->
            inboxListAdapter.setInboxItems(inboxList)
        })
//        supportActionBar.let {
//            binding.toolbar.visibility = View.GONE
//        }


    }

    private fun prepareRecyclerView() {
        inboxListAdapter = CastledInboxAdapter(this)
        binding.inboxRecyclerView.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = inboxListAdapter
            binding.inboxRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val firstVisibleItemPosition =
                        (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    val lastVisibleItemPosition =
                        (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

                    for (i in firstVisibleItemPosition..lastVisibleItemPosition) {
                        displayedItems.add(inboxListAdapter.inboxItemsList[i])
                        // Assuming you have a data structure associated with your RecyclerView
//                        val data = inboxListAdapter.getItem(i) // Replace with your adapter and data structure
//                        displayedItems.add(data)
                    }

                    // Now, 'displayedItems' contains the data associated with the currently displayed items in the RecyclerView as the user scrolls.
                }
            })

        }


    }
}
