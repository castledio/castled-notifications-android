package io.castled.android.notifications.workmanager

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

internal class CastledTokenRefreshWorkManager private constructor(context: Context) {

    private val workManager = WorkManager.getInstance(context)
    private val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true).build()

    fun init() {
        val refreshTokenRequest: PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<CastledTokenRefreshWorker>(
                repeatInterval = 14,
                repeatIntervalTimeUnit = TimeUnit.DAYS
            ).setConstraints(constraints)
                .build()

        workManager.enqueueUniquePeriodicWork(
            CASTLED_TOKEN_REFRESH_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            refreshTokenRequest
        )
    }

    companion object {

        private const val CASTLED_TOKEN_REFRESH_WORK = "castled_token_refresh_work"
        private var instance: CastledTokenRefreshWorkManager? = null

        @Synchronized
        fun getInstance(context: Context): CastledTokenRefreshWorkManager {
            if (instance == null) {
                instance = CastledTokenRefreshWorkManager(context)
            }
            return instance!!
        }
    }
}
