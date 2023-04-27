package io.castled.notifications.push

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import io.castled.notifications.logger.CastledLogger
import io.castled.notifications.logger.LogTags
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Suppress("unused")
class FcmTokenProvider(context: Context) : CastledPushTokenProvider(context) {

    private val logger = CastledLogger.getInstance(LogTags.PUSH_TOKEN_PROVIDER)

    override fun register(context: Context) {
        // No explicit register required
    }

    override suspend fun getToken(context: Context) = suspendCoroutine { continuation ->
        if (FirebaseApp.getApps(context).isEmpty()) {
            logger.debug("Fcm token fetch failed! Please make sure Firebase is initialized")
            continuation.resume(null)
            return@suspendCoroutine
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                continuation.resume(task.result)
            } else {
                task.exception?.let { logger.error("Fcm token fetch failed!", it) }
                continuation.resume(null)
            }
        }
    }

    override fun unregister(context: Context) {
        // No explicit un-registering required
    }
}