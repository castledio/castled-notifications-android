package io.castled.android.notifications.push

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.castled.android.notifications.commons.CastledClickActionUtils
import io.castled.android.notifications.logger.CastledLogger.Companion.getInstance
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.push.models.CastledClickAction
import io.castled.android.notifications.push.models.PushConstants
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class CastledNotificationReceiverAct : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(this, intent)
        finish()
    }

    private fun handleIntent(context: Context, intent: Intent) {
        try {
            logger.verbose("In push notification receiver activity")

            val contextJson =
                intent.extras?.getString(PushConstants.CASTLED_EXTRA_NOTIF_CONTEXT) ?: return
            val notificationContext: NotificationActionContext = Json.decodeFromString(contextJson)
            val clickedAction =
                notificationContext.actionType?.let { CastledClickAction.valueOf(it) } ?: CastledClickAction.NONE

            // Cancel the notification
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationContext.notificationId)

            when (clickedAction) {
                CastledClickAction.DEEP_LINKING, CastledClickAction.RICH_LANDING -> {
                    val uri = notificationContext.actionUri ?: return
                    CastledClickActionUtils.handleDeeplinkAction(context, uri, notificationContext.keyVals)
                }
                CastledClickAction.NAVIGATE_TO_SCREEN -> {
                    val className = notificationContext.actionUri ?: return
                    CastledClickActionUtils.handleNavigationAction(context, className, notificationContext.keyVals)
                }
                CastledClickAction.DEFAULT -> {
                    CastledClickActionUtils.handleDefaultAction(context)
                }
                else -> {
                    logger.debug("Undefined click action: $clickedAction")
                }
            }
            PushNotification.reportPushEvent(notificationContext)
        } catch (e: Exception) {
            logger.error("Push notification receiver activity failed!", e)
        }
    }

    companion object {
        private val logger = getInstance(LogTags.PUSH)
    }
}