package io.castled.android.notifications.push.views

interface PushServiceListener {
    fun onServiceStarted()
    fun onServiceTimerUpdated(millisUntilFinished: Long)
    fun onServiceTimerFinished()
    fun onBinderServiceConnected()
    fun onBinderServiceDisconnected()
}