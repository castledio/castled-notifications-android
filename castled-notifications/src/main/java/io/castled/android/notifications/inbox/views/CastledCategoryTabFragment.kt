package io.castled.android.notifications.inbox.views

import SwipeToDeleteCallback
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.castled.android.notifications.commons.ColorUtils
import io.castled.android.notifications.databinding.CastledInboxCategoryFragmentBinding
import io.castled.android.notifications.inbox.viewmodel.InboxViewModel
import io.castled.android.notifications.store.models.Inbox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class CastledCategoryTabFragment : Fragment() {

    private lateinit var binding: CastledInboxCategoryFragmentBinding
    private lateinit var viewModel: InboxViewModel
    private lateinit var context: Context
    private lateinit var inboxListAdapter: CastledInboxRecycleViewAdapter
    private var currentCategoryIndex: Int = 0
    private lateinit var currentCategory: String
    private var isItemsLoaded = false

    companion object {
        private const val ARG_INDEX = "index"
        private const val ARG_CAT = "category"

        fun newInstance(index: Int, category: String): CastledCategoryTabFragment {
            val fragment = CastledCategoryTabFragment()
            val args = Bundle()
            args.putInt(ARG_INDEX, index)
            args.putString(ARG_CAT, category)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = CastledInboxCategoryFragmentBinding.inflate(layoutInflater)
        context = requireContext()
        viewModel = ViewModelProvider(requireActivity())[InboxViewModel::class.java]
        viewModel.displayConfig?.let {
            customizeViews()
        }
        currentCategoryIndex = arguments?.getInt(ARG_INDEX, 0) ?: 0
        currentCategory = arguments?.getString(ARG_CAT, "") ?: ""
        prepareRecyclerView()
        isItemsLoaded = false
        initializeDaoListener()
        return binding.root
    }

    private fun customizeViews() {
        binding.viewBg.setBackgroundColor(
            ColorUtils.parseColor(
                viewModel.displayConfig!!.inboxViewBackgroundColor,
                Color.WHITE
            )
        )
        binding.txtEmptyView.text = viewModel.displayConfig!!.emptyMessageViewText
        binding.txtEmptyView.setTextColor(
            ColorUtils.parseColor(
                viewModel.displayConfig!!.emptyMessageViewTextColor,
                Color.BLACK
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        isItemsLoaded = false
        inboxListAdapter?.let {
            inboxListAdapter.reloadRecyclerView()
        }
    }

    private fun initializeDaoListener() {
        viewModel.inboxRepository.observeInboxLiveDataWithTag(
            if (currentCategoryIndex == 0) ""
            else currentCategory
        )
            .observe(viewLifecycleOwner) { inboxList ->
                inboxListAdapter.setInboxItems(inboxList)
                if (isItemsLoaded) {
                    (context as? CastledInboxActivity)?.refreshTabsAfterDBChanges()
                }
                isItemsLoaded = true
                binding.inboxRecycleView.visibility =
                    if (inboxList.isEmpty()) View.GONE else View.VISIBLE
                binding.txtEmptyView.visibility =
                    if (inboxList.isEmpty()) View.VISIBLE else View.GONE

            }
    }

    private fun prepareRecyclerView() {
        inboxListAdapter = CastledInboxRecycleViewAdapter(context, viewModel, this)
        binding.inboxRecycleView.adapter = inboxListAdapter
        binding.inboxRecycleView.layoutManager = LinearLayoutManager(context)
        val itemTouchHelper =
            ItemTouchHelper(SwipeToDeleteCallback(inboxListAdapter as CastledInboxRecycleViewAdapter))
        itemTouchHelper.attachToRecyclerView(binding.inboxRecycleView)
        binding.inboxRecycleView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val firstVisibleItemPosition =
                    (binding.inboxRecycleView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                val lastVisibleItemPosition =
                    (binding.inboxRecycleView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                for (i in firstVisibleItemPosition..lastVisibleItemPosition) {
                    if (i >= 0 && i < inboxListAdapter.inboxItemsList.size) {
                        val data = inboxListAdapter.inboxItemsList[i]
                        if (!data.isRead && !viewModel.displayedItems.contains(data.messageId)) {
                            viewModel.displayedItems.add(data.messageId)
                        }
                    }
                }

            }
        })

        binding.swipeRefreshList.setOnRefreshListener {
            (context as CastledInboxActivity).launch(Dispatchers.Default) {
                viewModel.inboxRepository.refreshInbox()
                launch(Dispatchers.IO) {
                    binding.swipeRefreshList.isRefreshing = false
                }
            }
        }
        binding.swipeRefreshList.setOnRefreshListener {
            (context as CastledInboxActivity).launch(Dispatchers.Default) {
                viewModel.inboxRepository.refreshInbox()
                launch(Dispatchers.IO) {
                    binding.swipeRefreshList.isRefreshing = false
                }
            }
        }
    }

    internal fun deleteItem(item: Inbox) {
        (context as CastledInboxActivity).launch(Dispatchers.IO) {
            item.isDeleted = true
            viewModel.inboxRepository.inboxDao.updateInboxItem(item)
        }
        viewModel.inboxViewLifecycleListener.deleteItem(item)
    }
}