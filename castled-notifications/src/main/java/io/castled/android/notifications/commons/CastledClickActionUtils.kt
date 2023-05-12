package io.castled.android.notifications.commons

import android.content.Context
import android.content.Intent
import android.net.Uri

object CastledClickActionUtils {

    fun handleDeeplinkAction(context: Context, actionUri: String, keyVals: Map<String, String>?) {
        val uri = keyVals?.let { CastledMapUtils.mapToQueryParams(actionUri, it) } ?: actionUri
        Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            .apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(this)
            }
    }

    fun handleNavigationAction(
        context: Context,
        actionClassName: String,
        keyVals: Map<String, String>?
    ) {
        Intent(context, Class.forName(actionClassName)).apply {
            keyVals?.let { keyVals ->
                putExtras(CastledMapUtils.mapToBundle(keyVals))
            }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(this)
        }
    }

    fun handleDefaultAction(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        context.startActivity(intent)
    }

}