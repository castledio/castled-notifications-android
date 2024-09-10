package io.castled.android.geofencer.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat

internal class PermissionValidator(private val context: Context) {

    private val requiredPermissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        Manifest.permission.RECEIVE_BOOT_COMPLETED
    )

    internal fun hasLocationPermission(context: Activity): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
    }


    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        if (requestCode == LOCATION_REQUEST_CODE) {
            return grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        }
        return false
    }

    fun validatePermissions(): Boolean {
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS
        )
        val declaredPermissions = packageInfo.requestedPermissions?.toList() ?: emptyList()

        for (permission in requiredPermissions) {
            if (!declaredPermissions.contains(permission)) {
                //    Log.e("PermissionValidator", "Missing permission: $permission")
                return false
            }
        }

        return true
    }

    companion object {
        private const val LOCATION_REQUEST_CODE = 1001
        private const val REQUEST_CODE_LOCATION_PERMISSION = 1002

        internal fun hasLocationPermission(context: Context): Boolean {
            val fineLocationPermission = ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            val coarseLocationPermission = ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            val backgroundLocationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10 (API level 29) and above
                ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                // For Android 9 (API level 28) and below, background location access is included in fine location
                true
            }

            return fineLocationPermission && coarseLocationPermission && backgroundLocationPermission
        }

        fun requestLocationPermission(activity: Activity) {
            val permissions = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Add background location permission for Android 10 and above
                permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }

            ActivityCompat.requestPermissions(
                activity,
                permissions.toTypedArray(),
                REQUEST_CODE_LOCATION_PERMISSION
            )
        }
    }


}