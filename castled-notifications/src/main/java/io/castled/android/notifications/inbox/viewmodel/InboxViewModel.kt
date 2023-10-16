package io.castled.android.notifications.inbox.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.castled.android.notifications.inbox.InboxLifeCycleListenerImpl
import io.castled.android.notifications.inbox.model.CastledInboxDisplayConfig

internal class InboxViewModel(application: Application) : AndroidViewModel(application) {
    val inboxRepository: InboxRepository
    val inboxViewLifecycleListener: InboxLifeCycleListenerImpl
    var currentCategoryIndex: Int = 0
    val displayedItems = mutableSetOf<Long>()
    var displayConfig: CastledInboxDisplayConfig? = null

    init {
        inboxRepository = InboxRepository(application)
        inboxViewLifecycleListener = InboxLifeCycleListenerImpl(application)
    }

}