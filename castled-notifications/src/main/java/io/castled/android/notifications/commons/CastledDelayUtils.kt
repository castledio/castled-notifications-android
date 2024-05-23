package io.castled.android.notifications.commons

import kotlinx.coroutines.delay

internal object CastledDelayUtils {

    suspend fun waitForCondition(
        checkIntervalMillis: Long,
        timeoutMillis: Long,
        condition: () -> Boolean,
    ): Boolean {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            if (condition()) {
                return true
            }
            delay(checkIntervalMillis)
        }
        return false
    }

}