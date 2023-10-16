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

internal class CastledCategoryTabFragment : Fragment() {


    private lateinit var binding: CastledInboxCategoryFragmentBinding
    private lateinit var viewModel: InboxViewModel
    private lateinit var context: Context
    private lateinit var inboxListAdapter: CastledInboxRecycleViewAdapter
    private var currentCategoryIndex: Int = 0
    private lateinit var currentCategory: String

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
    ): View? {

        binding = CastledInboxCategoryFragmentBinding.inflate(layoutInflater)
        context = requireContext()
        viewModel = ViewModelProvider(this)[InboxViewModel::class.java]
        viewModel.displayConfig?.let {
            customizeViews()
        }
        currentCategoryIndex = arguments?.getInt(ARG_INDEX, 0) ?: 0
        currentCategory = arguments?.getString(ARG_CAT, "") ?: ""
        println("viewModel.displayedItems count  ${viewModel.displayedItems.count()}--- $viewModel.displayedItems")
        prepareRecyclerView()
        initializeDaoListener()
        return binding.root
    }

    private fun customizeViews() {
        binding.inboxRecycleView.setBackgroundColor(
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
        binding.progressBar.setBackgroundColor(
            ColorUtils.parseColor(
                viewModel.displayConfig!!.navigationBarBackgroundColor,
                Color.BLUE
            )
        )
    }

    fun initializeDaoListener() {
        viewModel.inboxRepository.observeInboxLiveDataWithTag(
            if (currentCategoryIndex == 0) ""
            else currentCategory
        )
            .observe(viewLifecycleOwner) { inboxList ->
                viewModel.inboxRepository.cachedInboxItems.clear()
                viewModel.inboxRepository.cachedInboxItems.addAll(inboxList)
                inboxListAdapter.setInboxItems(inboxList)
                binding.inboxRecycleView.visibility =
                    if (inboxList.isEmpty()) View.GONE else View.VISIBLE
                binding.txtEmptyView.visibility =
                    if (inboxList.isEmpty()) View.VISIBLE else View.GONE


            }
    }

    private fun prepareRecyclerView() {
        inboxListAdapter = CastledInboxRecycleViewAdapter(context, viewModel)
        binding.inboxRecycleView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = inboxListAdapter
            val itemTouchHelper =
                ItemTouchHelper(SwipeToDeleteCallback(adapter as CastledInboxRecycleViewAdapter))
            itemTouchHelper.attachToRecyclerView(binding.inboxRecycleView)
            binding.inboxRecycleView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val firstVisibleItemPosition =
                        (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    val lastVisibleItemPosition =
                        (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                    for (i in firstVisibleItemPosition..lastVisibleItemPosition) {
                        if (i >= 0 && i < inboxListAdapter.inboxItemsList.size) {
                            val data = inboxListAdapter.inboxItemsList[i]
                            viewModel.displayedItems.add(data.messageId)
                            //  if (!data.isRead && !viewModel.displayedItems.contains(data.messageId))
//                            {
//                                viewModel.displayedItems.add(data.messageId)
//                            }
                        }
                    }

                }
            })
        }
    }

    internal fun deleteItem(position: Int, item: Inbox) {
        /*  binding.progressBar.visibility = ProgressBar.VISIBLE
          AppInbox.deleteInboxItem(item.toInboxItem()) { success, message ->
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
          }*/
    }

    internal fun onClicked(
        inboxItem: Inbox, actionParams: Map<String, Any>
    ) {
        viewModel.inboxViewLifecycleListener.onClicked(inboxItem, actionParams)
    }
}