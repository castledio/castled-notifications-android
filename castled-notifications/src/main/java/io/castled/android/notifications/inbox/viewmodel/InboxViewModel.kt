package io.castled.android.notifications.inbox.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.castled.android.notifications.inbox.InboxLifeCycleListenerImpl

internal class InboxViewModel(application: Application) : AndroidViewModel(application) {
    val inboxRepository: InboxRepository
    val inboxViewLifecycleListener: InboxLifeCycleListenerImpl
    var currentCategoryIndex: Int = 0

    init {
        inboxRepository = InboxRepository(application)
        inboxViewLifecycleListener = InboxLifeCycleListenerImpl(application)
    }

}