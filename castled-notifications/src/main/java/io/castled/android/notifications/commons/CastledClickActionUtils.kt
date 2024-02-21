package io.castled.android.notifications.commons

import android.content.Context
import android.content.Intent
import android.net.Uri
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.store.CastledSharedStore

object CastledClickActionUtils {
    private val logger = CastledLogger.getInstance(LogTags.IN_APP_TRIGGER)

    fun handleDeeplinkAction(context: Context, actionUri: String, keyVals: Map<String, String>?) {
        if (CastledSharedStore.configs.skipUrlHandling) {
            return
        }
        try {
            val uri = keyVals?.let { CastledMapUtils.mapToQueryParams(actionUri, it) } ?: actionUri
            Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                .apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                    context.startActivity(this)
                }
        } catch (e: Exception) {
            logger.error("Couldn't load  the  Activity!", e)
            handleDefaultAction(context)
        }

    }

    fun handleNavigationAction(
        context: Context,
        actionClassName: String,
        keyVals: Map<String, String>?
    ) {
        if (CastledSharedStore.configs.skipUrlHandling) {
            return
        }
        try {
            Intent(context, Class.forName(actionClassName)).apply {
                keyVals?.let { keyVals ->
                    putExtras(CastledMapUtils.mapToBundle(keyVals))
                }
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(this)
            }
        } catch (e: Exception) {
            logger.error("Couldn't load  the  Activity!", e)
            handleDefaultAction(context)
        }

    }

    fun handleDefaultAction(context: Context) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            intent?.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            context.startActivity(intent)
        } catch (e: Exception) {
            logger.error("Couldn't load  the  Activity!", e)
        }

    }

}