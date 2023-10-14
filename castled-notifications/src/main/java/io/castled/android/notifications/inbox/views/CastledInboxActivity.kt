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
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.castled.android.notifications.R
import io.castled.android.notifications.commons.ColorUtils
import io.castled.android.notifications.databinding.ActivityCastledInboxBinding
import io.castled.android.notifications.inbox.model.CastledInboxDisplayConfig
import io.castled.android.notifications.inbox.viewmodel.InboxRepository
import io.castled.android.notifications.inbox.viewmodel.InboxViewModel
import io.castled.android.notifications.store.models.Inbox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.Serializable


internal class CastledInboxActivity : AppCompatActivity(),
    CoroutineScope by CoroutineScope(Dispatchers.Main) {
    val displayedItems = mutableSetOf<Inbox>()

    private lateinit var viewModel: InboxViewModel
    private lateinit var binding: ActivityCastledInboxBinding
    private lateinit var inboxRepository: InboxRepository
    private var categories = mutableListOf<String>()
    private lateinit var categoriesTab: TabLayout
    private lateinit var viewPager: ViewPager2
    private var selectedTabColor = Color.BLACK
    private var unselectedTabColor = Color.WHITE
    private var selectedTabTextColor = 0XFF0000 // Red text for selected tab
    private var unselectedTabTextColor = 0X0000FF // Blue text for unselected tab
    private var selectedIndicatorColor = 0X0000FF // Blue text for unselected tab
    private var marginInPixels = 5

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCastledInboxBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[InboxViewModel::class.java]
        inboxRepository = viewModel.inboxRepository
        categoriesTab = binding.categoriesTab
        viewPager = binding.categoriesViewPager
        viewPager.isUserInputEnabled = false;
        binding.imgClose.setOnClickListener { finishAfterTransition() }
        val displayConfig = getSerializable(
            this, "displayConfig",
            CastledInboxDisplayConfig::class.java
        )
        displayConfig?.let {
            customizeViews(displayConfig)
        } ?: run {
            supportActionBar?.let {
                binding.toolbar.visibility = View.GONE
            }
        }
        launch(Dispatchers.IO) {
            inboxRepository.refreshInbox()
            val newCategories = inboxRepository.getCategoryTags()
            launch(Dispatchers.Main) {
                categories.clear()
                categories.addAll(newCategories)
                prepareViewPager()
            }
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
            Color.parseColor("#3366CC")
        )
        unselectedTabTextColor = ColorUtils.parseColor(
            displayConfig.tabBarDefaultTextColor,
            Color.BLACK
        )
        selectedIndicatorColor = ColorUtils.parseColor(
            displayConfig.tabBarIndicatorBackgroundColor,
            Color.parseColor("#3366CC")
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
            viewModel.inboxViewLifecycleListener.registerReadEvents(displayedItems)
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

    private fun prepareViewPager() {

        marginInPixels = (2.5 * Resources.getSystem().displayMetrics.density).toInt()
        categoriesTab.setSelectedTabIndicatorColor(selectedIndicatorColor)
        if (categories.count() == 1) {
            binding.categoriesTabParentView.visibility = View.GONE
        }
        val pagerAdapter = PagerAdapter(this)
        viewPager.adapter = pagerAdapter
        TabLayoutMediator(categoriesTab, viewPager) { tab, position ->
            // Set the title for each tab here
            tab.text = categories[position]
        }.attach()
        if (areTabTitlesExceedingWidth(categoriesTab, resources.displayMetrics.widthPixels)) {
            categoriesTab.tabMode = TabLayout.MODE_SCROLLABLE
        } else {
            categoriesTab.tabMode = TabLayout.MODE_FIXED
        }
        for (i in 0 until categoriesTab.tabCount) {
            val tab = categoriesTab.getTabAt(i)
            tab?.view?.tab?.setCustomView(R.layout.castled_custom_tab)
            setTab(tab, i == viewModel.currentCategoryIndex)

        }
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

    private fun areTabTitlesExceedingWidth(tabLayout: TabLayout, availableWidth: Int): Boolean {
        var totalWidth = 0
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            totalWidth += tab?.text?.length ?: 0
        }
        return totalWidth > availableWidth
    }

    private inner class PagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

        override fun getItemCount(): Int {
            return categories.size
        }

        override fun createFragment(position: Int): Fragment {
            return CastledCategoryTabFragment.newInstance(position, categories[position])
        }
    }
}
