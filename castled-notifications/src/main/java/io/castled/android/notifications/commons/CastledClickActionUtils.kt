package io.castled.android.notifications.commons

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.castled.android.notifications.inapp.InAppNotification
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags


object CastledClickActionUtils {
    private val logger = CastledLogger.getInstance(LogTags.IN_APP_TRIGGER)

    fun handleDeeplinkAction(context: Context, actionUri: String, keyVals: Map<String, String>?) {
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

    fun handlePushPermissionAction() {
        InAppNotification.getCurrentActivity()?.let {
            // This is only necessary for API level >= 33 (TIRAMISU)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(it, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    logger.debug("PERMISSION_GRANTED")
                } else {

                    // Directly ask for the permission
                    ActivityCompat.requestPermissions(
                        it,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        101
                    )
                }
            }
        }
    }
}