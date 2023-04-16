package io.castled.notifications.globals

import kotlinx.coroutines.sync.Mutex

internal object CastledGlobals {

    val retryDbMutex = Mutex()

}