package io.castled.android.notifications.inapp

interface InAppViewBaseDecorator {

    fun show(withApiCall: Boolean)

    fun close()
}