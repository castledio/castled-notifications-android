package io.castled.android.notifications.inbox.views

import android.app.Activity
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.castled.android.notifications.R
import io.castled.android.notifications.commons.ColorUtils
import io.castled.android.notifications.databinding.ActivityCastledInboxBinding
import io.castled.android.notifications.inbox.model.CastledInboxDisplayConfig
import io.castled.android.notifications.inbox.model.InboxConstants
import io.castled.android.notifications.inbox.viewmodel.InboxRepository
import io.castled.android.notifications.inbox.viewmodel.InboxViewModel
import io.castled.android.notifications.store.CastledSharedStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.Serializable


internal class CastledInboxActivity : AppCompatActivity(),
    CoroutineScope by CoroutineScope(Dispatchers.Main) {
    private val SELECTED_COLOR = "#3366CC"
    private val viewModel: InboxViewModel by viewModels()
    private val allString = "All"
    private lateinit var binding: ActivityCastledInboxBinding
    private lateinit var inboxRepository: InboxRepository
    private var categories = mutableListOf<String>()
    private lateinit var categoriesTab: TabLayout
    private lateinit var viewPager: ViewPager2
    private var selectedTabColor = Color.WHITE
    private var unselectedTabColor = Color.WHITE
    private var selectedTabTextColor = Color.parseColor(SELECTED_COLOR)
    private var unselectedTabTextColor = Color.BLACK
    private var selectedIndicatorColor = Color.parseColor(SELECTED_COLOR)
    private var marginInPixels = 5

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityCastledInboxBinding.inflate(layoutInflater)
        setContentView(binding.root)
        inboxRepository = viewModel.inboxRepository
        categoriesTab = binding.categoriesTab
        viewPager = binding.categoriesViewPager
        viewPager.isUserInputEnabled = false

        val displayConfig = getSerializable(
            this, InboxConstants.CASTLED_DISPLAY_CONFIGS,
            CastledInboxDisplayConfig::class.java
        )
        displayConfig?.let {
            viewModel.displayConfig = displayConfig
            customizeViews(displayConfig)
            populateTabs(displayConfig.showCategoriesTab, true)
        } ?: run {
            supportActionBar?.let {
                binding.toolbar.visibility = View.GONE
            }
            populateTabs(withTab = true, shouldRefresh = true)
        }
    }

    private fun populateTabs(withTab: Boolean, shouldRefresh: Boolean) {

        if (shouldRefresh) {
            launch(Dispatchers.Default) {
                CastledSharedStore.getUserId()?.let { inboxRepository.refreshInbox() }

            }
        }
        launch(Dispatchers.IO) {

            if (withTab) {
                val newCategories = listOf(allString) + inboxRepository.getCategoryTags()
                if (newCategories != categories) {
                    categories.clear()
                    launch(Dispatchers.Main) {
                        categories.addAll(newCategories)
                        prepareViewPager()
                    }
                }

            } else {
                categories.clear()
                categories.add(allString)
                launch(Dispatchers.Main) {
                    prepareViewPager()
                }
            }
        }
    }

    internal fun refreshTabsAfterDBChanges() {
        if (categoriesTab.visibility == View.VISIBLE) {
            populateTabs(withTab = true, shouldRefresh = false)
        }
    }

    private fun customizeViews(displayConfig: CastledInboxDisplayConfig) {

        selectedTabColor = ColorUtils.parseColor(
            displayConfig.tabBarSelectedBackgroundColor,
            Color.WHITE
        )
        unselectedTabColor = ColorUtils.parseColor(
            displayConfig.tabBarDefaultBackgroundColor,
            Color.WHITE
        )
        selectedTabTextColor = ColorUtils.parseColor(
            displayConfig.tabBarSelectedTextColor,
            Color.parseColor(SELECTED_COLOR)
        )
        unselectedTabTextColor = ColorUtils.parseColor(
            displayConfig.tabBarDefaultTextColor,
            Color.BLACK
        )
        selectedIndicatorColor = ColorUtils.parseColor(
            displayConfig.tabBarIndicatorBackgroundColor,
            Color.parseColor(SELECTED_COLOR)
        )

        binding.toolbar.setBackgroundColor(
            ColorUtils.parseColor(
                displayConfig.navigationBarBackgroundColor,
                Color.WHITE
            )
        )

        binding.categoriesTabParentView.visibility =
            if (displayConfig.showCategoriesTab) View.VISIBLE else View.GONE
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
        binding.categoriesTab.setBackgroundColor(unselectedTabColor)
        binding.toolbar.visibility =
            if (displayConfig.hideNavigationBar) View.GONE else View.VISIBLE

        binding.imgClose.setOnClickListener { dismissInboxActivity() }

        if (displayConfig.hideBackButton) {
            binding.imgClose.visibility = View.GONE
            binding.toolbarTitle.setPadding(
                0,
                binding.toolbarTitle.paddingTop,
                binding.toolbarTitle.paddingRight,
                binding.toolbarTitle.paddingBottom
            )
        }

        val backButtonResourceId = getBackButtonResId(displayConfig.backButtonResourceId)

        binding.imgClose.setImageResource(backButtonResourceId)
        val actionBar = supportActionBar
        actionBar?.let {
            actionBar.setBackgroundDrawable(
                ColorDrawable(
                    ColorUtils.parseColor(
                        displayConfig.navigationBarBackgroundColor,
                        (android.R.attr.colorPrimary)
                    )
                )
            )
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
            if (!displayConfig.hideBackButton) {
                it.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeAsUpIndicator(backButtonResourceId)
            }
            binding.toolbar.visibility = View.GONE
        }
    }

    override fun onPause() {
        super.onPause()
        if (viewModel.displayedItems.isNotEmpty()) {
            viewModel.inboxViewLifecycleListener.registerReadEvents(viewModel.displayedItems)
        }
        // Perform actions when the fragment is no longer visible
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.itemId

        // Handle the up button
        if (id == android.R.id.home) {
            // Perform your desired action here (e.g., navigating up or closing the activity)
            dismissInboxActivity()
            return true
        }

        return super.onOptionsItemSelected(item)
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

    private fun prepareViewPager() {

        marginInPixels = (2.5 * Resources.getSystem().displayMetrics.density).toInt()
        categoriesTab.setSelectedTabIndicatorColor(selectedIndicatorColor)
        if (categories.count() == 1) {
            binding.categoriesTabParentView.visibility = View.GONE
        } else {
            //scenario - initially only one tab, later adding more tabs
            viewModel.displayConfig?.let {
                if (viewModel.displayConfig!!.showCategoriesTab) {
                    binding.categoriesTabParentView.visibility = View.VISIBLE

                }
            }
        }
        val pagerAdapter = PagerAdapter(this)
        viewPager.adapter = pagerAdapter
        // viewPager.offscreenPageLimit = min(categories.size, 3)

        TabLayoutMediator(categoriesTab, viewPager) { tab, position ->
            // Set the title for each tab here
            tab.text = categories[position]
        }.attach()
        for (i in 0 until categoriesTab.tabCount) {
            val tab = categoriesTab.getTabAt(i)
            tab?.view?.tab?.setCustomView(R.layout.castled_custom_tab)
            setTab(tab, i == viewModel.currentCategoryIndex)

        }
        setModeForTabView()
        // Set up a TabLayout.OnTabSelectedListener to change tab colors when selected
        categoriesTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewModel.currentCategoryIndex = tab.position
                setTab(tab, true)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                setTab(tab, false)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // Tab is reselected
            }
        })
    }

    private fun setTab(tab: TabLayout.Tab?, selected: Boolean) {

        if (selected)
            tab?.view?.setPadding(0, 0, 0, marginInPixels)
        else
            tab?.view?.setPadding(0, 0, 0, 0)

        tab?.view?.findViewById<TextView>(android.R.id.text1)
            ?.setBackgroundColor(if (selected) selectedTabColor else unselectedTabColor)
        tab?.view?.findViewById<TextView>(android.R.id.text1)
            ?.setTextColor(if (selected) selectedTabTextColor else unselectedTabTextColor)

    }

    private fun setModeForTabView() {
        categoriesTab.post {
            // Calculate the total content width

            val screenWidth = resources.displayMetrics.widthPixels

            var totalContentWidth = 0
            for (i in 0 until categoriesTab.tabCount) {
                val tabView = categoriesTab.getTabAt(i)?.view
                totalContentWidth += tabView?.measuredWidth ?: 0
            }

            // Set the tabMode based on content width
            if (totalContentWidth > screenWidth) {
                categoriesTab.tabMode = TabLayout.MODE_SCROLLABLE
            } else {
                categoriesTab.tabMode = TabLayout.MODE_FIXED
            }
        }
    }

    private inner class PagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

        override fun getItemCount(): Int {
            return categories.size
        }

        override fun createFragment(position: Int): Fragment {
            return CastledCategoryTabFragment.newInstance(position, categories[position])
        }
    }

    private fun dismissInboxActivity() {
        finishAfterTransition()
    }

    private fun getBackButtonResId(resourceId: Int?): Int {
        return if (resourceId != null && isValidResourceId(resourceId)) {
            resourceId // Return the provided resource ID if it's not null and valid
        } else {
            R.drawable.castled_default_back // Return the default (invalid) resource ID
        }
    }

    private fun isValidResourceId(resourceId: Int): Boolean {
        return try {
            // Attempt to retrieve the resource
            val drawable = ResourcesCompat.getDrawable(resources, resourceId, null)
            // Check if the resource is not null
            drawable != null
        } catch (e: Resources.NotFoundException) {
            // Catch the exception if the resource is not found
            false
        }
    }

}
