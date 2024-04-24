package io.castled.android.notifications.globals

import kotlinx.coroutines.sync.Mutex

internal object CastledGlobals {

    val networkWorkDbMutex = Mutex()
}