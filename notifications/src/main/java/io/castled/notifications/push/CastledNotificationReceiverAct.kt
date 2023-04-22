package io.castled.notifications.push

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.castled.notifications.commons.CastledMapUtils
import io.castled.notifications.logger.CastledLogger.Companion.getInstance
import io.castled.notifications.logger.LogTags
import io.castled.notifications.push.PushNotification.reportPushEvent
import io.castled.notifications.push.models.ClickAction
import io.castled.notifications.push.models.NotificationActionContext
import io.castled.notifications.push.models.PushConstants
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class CastledNotificationReceiverAct : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(this, intent)
    }

    private fun handleIntent(context: Context, intent: Intent) {
        try {
            logger.verbose("In push notification receiver activity")

            val contextJson =
                intent.extras?.getString(PushConstants.CASTLED_EXTRA_NOTIF_CONTEXT) ?: return
            val notificationContext: NotificationActionContext = Json.decodeFromString(contextJson)
            val clickedAction =
                notificationContext.actionType?.let { ClickAction.valueOf(it) } ?: ClickAction.NONE

            // Cancel the notification
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationContext.notificationId)

            val clientIntent = when (clickedAction) {
                ClickAction.DEEP_LINKING, ClickAction.RICH_LANDING -> {
                    val uri = notificationContext.actionUri?.let { actionUri ->
                        notificationContext.keyVals?.let { keyVals ->
                            CastledMapUtils.mapToQueryParams(actionUri, keyVals)
                        } ?: actionUri
                    }
                    Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                }
                ClickAction.NAVIGATE_TO_SCREEN -> {
                    val className = notificationContext.actionUri ?: return
                    Intent(context, Class.forName(className)).apply {
                        notificationContext.keyVals?.let { keyVals ->
                            putExtras(CastledMapUtils.mapToBundle(keyVals))
                        }
                    }
                }
                ClickAction.DEFAULT -> {
                    context.packageManager.getLaunchIntentForPackage(context.packageName)
                }
                else -> {
                    logger.debug("Undefined click action: $clickedAction")
                    null
                }
            }

            clientIntent?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(this)
            }
            reportEvent(notificationContext)
            finish()
        } catch (e: Exception) {
            logger.error("Push notification receiver activity failed!", e)
        }
    }

    private fun reportEvent(event: NotificationActionContext) {
        reportPushEvent(event)
    }

    companion object {
        private val logger = getInstance(LogTags.PUSH)
    }
}