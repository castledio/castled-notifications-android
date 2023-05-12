package io.castled.android.notifications.commons

import java.util.concurrent.atomic.AtomicInteger

internal object CastledIdUtils {

    private val c = AtomicInteger(0)
    val newId: Int
        get() = c.incrementAndGet()
}