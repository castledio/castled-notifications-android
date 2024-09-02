package io.castled.android.geofencer

import GeofenceLifeCycleListener
import android.annotation.SuppressLint
import android.content.Context
import io.castled.android.geofencer.models.GeofenceConfig
import io.castled.android.notifications.CastledNotifications
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin


class CastledGeofencer private constructor(private val context: Context) {

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: CastledGeofencer? = null
        fun getInstance(context: Context): CastledGeofencer {

            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CastledGeofencer(context).also { INSTANCE = it }
            }
        }

        private lateinit var externalScope: CoroutineScope
    }

    private var config: GeofenceConfig? = null
    private val logger: CastledLogger = CastledLogger.getInstance(LogTags.GEOFENCE_REPOSITORY)
    private var fetchJob: Job? = null
    private var enabled = false
    private var geofenceEventsListener: GeofenceLifeCycleListener? = null
    private val geofenceManager: GeofenceManagerImpl = GeofenceManagerImpl.getInstance(context)


    fun initialize(config: GeofenceConfig?) {
        if (!CastledNotifications.isInited()) {
            logger.debug("CastledNotifications is not initialized.")
            return
        }
        this.config = config ?: GeofenceConfig()
        geofenceManager.init(this.config!!)
        enabled = true
        logger.debug("Geofence module initialized")

    }

    internal suspend fun cancelInboxJob() {
        if (fetchJob != null && fetchJob!!.isActive) {
            fetchJob!!.cancelAndJoin()
        }
    }


    fun onStoreInitialized(context: Context) {
//        CastledSharedStore.getUserId()?.let {
//            startInboxJob()
//        }
    }

    fun onStoreUserIdSet(context: Context) {
        //  startInboxJob()
    }

    fun startMonitoring() {
        if (!enabled) {
            logger.debug("Initialize the Geofence module")
            return
        }
        geofenceManager.startMonitoring()
    }

    fun stopMonitoring() {
        if (!enabled) {
            logger.debug("Initialize the Geofence module")
            return
        }
        geofenceManager.stopMonitoring()
    }


}
