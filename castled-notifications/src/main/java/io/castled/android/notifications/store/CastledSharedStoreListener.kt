package io.castled.android.notifications.store

import android.content.Context

internal interface CastledSharedStoreListener {

    fun onStoreInitialized(context: Context) {}

    fun onStoreUserIdSet(context: Context) {}

}