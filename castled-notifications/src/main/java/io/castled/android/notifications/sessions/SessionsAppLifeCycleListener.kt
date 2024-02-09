package io.castled.android.notifications.sessions

import android.app.Activity
import io.castled.android.notifications.observer.CastledAppLifeCycleListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SessionsAppLifeCycleListener(private val castledScope: CoroutineScope) :
    CastledAppLifeCycleListener {

    override fun onAppMovedToForeground(activity: Activity) {
        castledScope.launch(Dispatchers.Default) {
            Sessions.didEnterForeground()
        }
    }

    override fun onAppMovedToBackground(activity: Activity) {
        Sessions.didEnterBackground()
    }

}