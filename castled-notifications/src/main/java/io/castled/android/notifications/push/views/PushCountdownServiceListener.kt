package io.castled.android.notifications.push.views

interface PushCountdownServiceListener {
    fun onServiceStarted()
    fun onServiceTimerUpdated(millisUntilFinished: Long)
    fun onServiceTimerFinished()
    fun onServiceConnected()
    fun onServiceDisconnected()
}