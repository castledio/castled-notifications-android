package io.castled.android.notifications.workmanager

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

internal class CastledPushWorkManager private constructor(context: Context) {

    private val workManager = WorkManager.getInstance(context)
    private val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .build()

    fun startTokenRefresh() {
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

    fun startPushBoostSync() {
        val refreshTokenRequest: PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<CastledPushBoostSyncWorker>(
                repeatInterval = 15,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            ).setConstraints(constraints)
                .build()

        workManager.enqueueUniquePeriodicWork(
            CASTLED_PUSH_BOOST_SYNC_WORK,
            ExistingPeriodicWorkPolicy.REPLACE,
            refreshTokenRequest
        )
    }

    companion object {

        private const val CASTLED_TOKEN_REFRESH_WORK = "castled_token_refresh_work"
        private const val CASTLED_PUSH_BOOST_SYNC_WORK = "castled_push_boost_sync_work"
        private var instance: CastledPushWorkManager? = null

        @Synchronized
        fun getInstance(context: Context): CastledPushWorkManager {
            if (instance == null) {
                instance = CastledPushWorkManager(context)
            }
            return instance!!
        }
    }
}
