package io.castled.android.notifications.tracking.device

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.pm.PackageInfoCompat
import io.castled.android.notifications.store.CastledSharedStore
import java.util.Locale
import kotlin.random.Random


internal class CastledDeviceDetails(context: Context) {

    private val packageManager by lazy { context.packageManager }
    private val packageName by lazy { context.packageName }

    internal fun getAppVersion(): String {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                packageManager.getPackageInfo(packageName, 0)
            }
            //  packageInfo.versionName
            PackageInfoCompat.getLongVersionCode(packageInfo).toString()

        } catch (e: Exception) {
            "0.0.0"
        }
    }

    fun checkNotificationPermissions(context: Context): Boolean {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13 and above
            if (manager.areNotificationsEnabled() && ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                return true
            }
        } else {
            // For Android 12 and below
            if (manager.areNotificationsEnabled()) {
                return true
            }
        }
        return false
    }

    internal fun getModel(): String {
        return Build.MODEL
    }

    internal fun getMake(): String {
        return Build.MANUFACTURER
    }

    internal fun getOSVersion(): String {
        return Build.VERSION.RELEASE
    }

    internal fun getLocale(): String {
        return Locale.getDefault().toString()
    }

    // You can obtain a unique device ID using various methods, such as the device's secure Android ID
    internal fun getDeviceId(): String {
        CastledSharedStore.getDeviceId()?.let {
            return it
        } ?: run {
            val randomString = Random.nextInt(1, Int.MAX_VALUE).toString()
            CastledSharedStore.setDeviceId(randomString)
            return randomString
        }

    }
}
