package io.castled.android.notifications.inbox

import android.app.Activity
import io.castled.android.notifications.observer.CastledAppLifeCycleListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InboxAppLifeCycleListener(private val castledScope: CoroutineScope) :
    CastledAppLifeCycleListener {

    override fun onAppMovedToForeground(activity: Activity) {
        castledScope.launch(Dispatchers.Default) {
            AppInbox.refreshInbox()
        }
    }
}