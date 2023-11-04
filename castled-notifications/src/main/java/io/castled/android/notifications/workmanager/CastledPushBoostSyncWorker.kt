package io.castled.android.notifications.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.castled.android.notifications.CastledNotifications
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.push.PushNotification
import io.castled.android.notifications.store.CastledSharedStore
import java.util.Calendar

class CastledPushBoostSyncWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) :
    CoroutineWorker(appContext, workerParams) {
    private val logger = CastledLogger.getInstance(LogTags.PUSH)
    override suspend fun doWork(): Result {
        return try {
            if (CastledSharedStore.getUserId().isNullOrBlank()) {
                logger.debug("UserId not set. Skipping push messages sync")
                Result.success()
            }
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val startDndHour = 22  // 10 PM
            val endDndHour = 6     // 6 AM

            // Check if the current time is within "Do Not Disturb" hours
            if (currentHour in endDndHour until startDndHour) {
                // Check for push messages
                val pushMessages = PushNotification.getPushMessages()
                pushMessages.forEach { CastledNotifications.handlePushNotification(appContext, it) }
                Result.success()
            } else {
                // Skip the task now
                Result.success()
            }
        } catch (e: Exception) {
            // Handle exceptions if any occurred during token retrieval
            logger.debug("Error syncing push messages: $e")
            Result.failure()
        }
    }
}