package io.castled.android.notifications.sessions

import android.app.Application
import android.content.Context
import io.castled.android.notifications.commons.CastledDelayUtils
import io.castled.android.notifications.commons.CastledUUIDUtils
import io.castled.android.notifications.commons.DateTimeUtils
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.observer.CastledLifeCycleObserver
import io.castled.android.notifications.sessions.events.CastledSessionEvent
import io.castled.android.notifications.sessions.service.SessionsRepository
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.store.CastledSharedStoreListener
import io.castled.android.notifications.store.consts.PrefStoreKeys
import io.castled.android.notifications.tracking.device.CastledDeviceDetails
import io.castled.android.notifications.tracking.events.extensions.toJsonElement
import io.castled.android.notifications.workmanager.models.CastledSessionRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import java.util.Date


internal object Sessions : CastledSharedStoreListener {

    private lateinit var sessionsRepository: SessionsRepository
    private val logger: CastledLogger = CastledLogger.getInstance(LogTags.SESSIONS)
    private lateinit var externalScope: CoroutineScope
    private lateinit var deviceInfo: CastledDeviceDetails

    var sessionId: String? = null
    private var sessionStartTime: Long = 0L
    private var sessionEndTime: Long = 0L
    internal var sessionDuration: Long = 0L
    private var isFirstSession: Boolean = true
    private var currentStartTime: Long = 0L
    private var isSessionStarted: Boolean = false
    private val sessionMutex = Mutex()

    fun init(application: Application, externalScope: CoroutineScope) {
        Sessions.externalScope = externalScope
        sessionsRepository = SessionsRepository(application)
        deviceInfo = CastledDeviceDetails(application)
        CastledLifeCycleObserver.registerListener(SessionsAppLifeCycleListener(externalScope))
        CastledSharedStore.registerListener(this)
    }

    internal suspend fun startCastledSession() {
        CastledDelayUtils.waitForCondition(500, 120 * 1000) {
            // Wait until device id is set (immediately after store init)
            // Once wait timeouts, proceed anyway
            !deviceInfo.getDeviceId().isNullOrEmpty()
        }
        sessionMutex.withLock {
            withContext(Dispatchers.Default) {
                currentStartTime = System.currentTimeMillis() / 1000
                initializeSessionDetails()
                if (!isInCurrentSession()) {
                    //new session
                    createNewSession()
                    resetTheValuesForNewSession()

                }
                isSessionStarted = true
            }
        }
    }

    private fun isInCurrentSession(): Boolean {
        return sessionEndTime != 0L && (System.currentTimeMillis() / 1000 - sessionEndTime) <= CastledSharedStore.configs.sessionTimeOutSec
    }

    private suspend fun createNewSession() {
        val sessionDetails = mutableListOf<CastledSessionEvent>()
        val properties = JsonObject(
            mapOf(
                "deviceId" to deviceInfo.getDeviceId().toJsonElement(),
                "platform" to "android".toJsonElement()

            )
        )
        if (!sessionId.isNullOrEmpty()) {
            val dateEnded =
                Date((if (sessionEndTime == 0L) System.currentTimeMillis() / 1000 else sessionEndTime).toLong() * 1000)
            val event = CastledSessionEvent(
                sessionId = sessionId!!,
                sessionEventType = SessionType.ENDED.type,
                userId = CastledSharedStore.getUserId() ?: "",
                timestamp = DateTimeUtils.getStringFromDate(dateEnded),
                duration = sessionDuration,
                properties = properties
            )
            sessionDetails.add(event)
            CastledSharedStore.setValue(PrefStoreKeys.SESSION_IS_FIRST, false)
            isFirstSession = false
        }
        sessionId = getCastledSessionId()
        sessionDuration = 0L
        sessionStartTime = System.currentTimeMillis() / 1000
        currentStartTime = sessionStartTime
        sessionEndTime = sessionStartTime

        val dateStarted =
            Date(currentStartTime * 1000)
        val event = CastledSessionEvent(
            sessionId = sessionId!!,
            sessionEventType = SessionType.STARTED.type,
            userId = CastledSharedStore.getUserId() ?: "",
            firstSession = isFirstSession,
            timestamp = DateTimeUtils.getStringFromDate(dateStarted),
            properties = properties
        )
        sessionDetails.add(event)
        sessionsRepository.reportSessionEvent(CastledSessionRequest(sessionDetails))
    }

    private fun resetTheValuesForNewSession() {
        CastledSharedStore.setValue(PrefStoreKeys.SESSION_ID, sessionId!!)
        CastledSharedStore.setValue(PrefStoreKeys.SESSION_DURATION, sessionDuration)
        CastledSharedStore.setValue(PrefStoreKeys.SESSION_START_TIME, sessionStartTime)
        CastledSharedStore.setValue(PrefStoreKeys.SESSION_END_TIME, sessionEndTime)
    }

    fun didEnterForeground() {
        externalScope.launch(Dispatchers.Default) {
            CastledSharedStore.getUserId()?.let {
                startCastledSession()
            }
        }
    }

    fun didEnterBackground() {
        if (!isSessionStarted || currentStartTime == 0L) {
            return
        }
        CastledSharedStore.getUserId()?.let {
            sessionEndTime = System.currentTimeMillis() / 1000
            sessionDuration += sessionEndTime - currentStartTime
            CastledSharedStore.setValue(PrefStoreKeys.SESSION_DURATION, sessionDuration)
            CastledSharedStore.setValue(PrefStoreKeys.SESSION_END_TIME, sessionEndTime)
            currentStartTime = sessionEndTime
        }
    }

    private fun initializeSessionDetails() {
        sessionId = CastledSharedStore.getValue(PrefStoreKeys.SESSION_ID, "")
        isFirstSession = CastledSharedStore.getValue(PrefStoreKeys.SESSION_IS_FIRST, true)
        sessionStartTime = CastledSharedStore.getValue(PrefStoreKeys.SESSION_START_TIME, 0L)
        sessionDuration = CastledSharedStore.getValue(PrefStoreKeys.SESSION_DURATION, 0L)
        sessionEndTime = CastledSharedStore.getValue(PrefStoreKeys.SESSION_END_TIME, 0L)
    }

    override fun onStoreUserIdSet(context: Context) {
        externalScope.launch(Dispatchers.Default) {
            startCastledSession()
        }
    }

    private fun getCastledSessionId() = CastledUUIDUtils.getIdBase64()
}
