package io.castled.android.notifications.commons
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build

internal class CastledManifestInfo(private val context: Context) {

    private val CASTLED_EXCLUDED_INAPP_ACTIVITIES = "CASTLED_EXCLUDED_INAPPS"

    private val appInfo: ApplicationInfo? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getApplicationInfo(
                    context.packageName,
                    PackageManager.GET_META_DATA
                )
            } else {
                context.packageManager.getApplicationInfo(
                    context.packageName,
                    PackageManager.GET_META_DATA
                )
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    fun getExcludedActivities(): List<String> {
        val metaData = appInfo?.metaData
        val metaDataValue = metaData?.getString(CASTLED_EXCLUDED_INAPP_ACTIVITIES)
        return metaDataValue?.split(",") ?: emptyList()
    }
 }
