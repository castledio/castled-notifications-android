package io.castled.android.notifications.push.views

interface PushCountdownServiceListener {
    fun onServiceStarted()
    fun onTimerUpdated(millisUntilFinished: Long)
    fun onTimerFinished()
}