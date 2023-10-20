package io.castled.android.notifications.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.messaging.FirebaseMessaging
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.push.models.PushTokenInfo
import io.castled.android.notifications.push.models.PushTokenType
import io.castled.android.notifications.push.service.PushRepository
import io.castled.android.notifications.store.CastledSharedStore
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CastledTokenRefreshWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) :
    CoroutineWorker(appContext, workerParams) {
    private val logger = CastledLogger.getInstance(LogTags.PUSH)
    override suspend fun doWork(): Result {
        return try {
            val token = getToken()
            if (!token.isNullOrEmpty()) {
                val appContext = appContext
                CastledSharedStore.init(appContext)
                val userId = CastledSharedStore.getUserId()
                if (!userId.isNullOrEmpty() &&
                    token != CastledSharedStore.getToken(PushTokenType.FCM)
                ) {
                    PushRepository(appContext).register(
                        userId,
                        listOf(PushTokenInfo(token, PushTokenType.FCM))
                    )
                    logger.debug("Fcm token refresh completed")
                }
                Result.success()
            } else {
                logger.debug("FCM registration token is empty!")
                Result.success()
            }
        } catch (e: Exception) {
            // Handle exceptions if any occurred during token retrieval
            logger.debug("Fetching Token retrieval error $e")
            Result.failure()
        }
    }

    private suspend fun getToken(): String? = suspendCoroutine { continuation ->
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                continuation.resume(token)
            } else {
                continuation.resume(null)
            }
        }
    }
}