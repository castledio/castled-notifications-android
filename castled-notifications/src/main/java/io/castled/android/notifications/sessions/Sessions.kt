package io.castled.android.notifications.sessions

import android.app.Application
import android.content.Context
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
import kotlinx.serialization.json.JsonObject
import java.util.Date
import java.util.UUID


internal object Sessions : CastledSharedStoreListener {

    private lateinit var sessionsRepository: SessionsRepository
    private val logger: CastledLogger = CastledLogger.getInstance(LogTags.SESSIONS)
    private lateinit var externalScope: CoroutineScope
    private lateinit var deviceInfo: CastledDeviceDetails

    var sessionId: String = ""
    private var sessionStartTime: Long = 0L
    private var sessionEndTime: Long = 0L
    private var sessionDuration: Long = 0L
    private var isFirstSession: Boolean = true
    private var currentStartTime: Long = 0L

    fun init(application: Application, externalScope: CoroutineScope) {
        Sessions.externalScope = externalScope
        sessionsRepository = SessionsRepository(application)
        deviceInfo = CastledDeviceDetails(application)
        CastledLifeCycleObserver.registerListener(SessionsAppLifeCycleListener(externalScope))
        CastledSharedStore.registerListener(this)
    }

    fun startCastledSession() {
        externalScope.launch(Dispatchers.Default) {
            currentStartTime = System.currentTimeMillis() / 1000
            initializeSessionDetails()
            if (!isInCurrentSession()) {
                //new session
                createNewSession()
                resetTheValuesForNewSession()

            }
        }
    }

    private fun isInCurrentSession(): Boolean {
        return sessionEndTime != 0L && (System.currentTimeMillis() / 1000 - sessionEndTime) <= CastledSharedStore.configs.sessionTimeOutSec
    }

    private suspend fun createNewSession() {
        val sessionDetails = mutableListOf<CastledSessionEvent>()
        val deviceId = JsonObject(
            mapOf(
                "deviceId" to deviceInfo.getDeviceId().toJsonElement()
            )
        )
        if (sessionId.isNotEmpty()) {
            val dateEnded =
                Date((if (sessionEndTime == 0L) System.currentTimeMillis() / 1000 else sessionEndTime).toLong() * 1000)
            val event = CastledSessionEvent(
                sessionId = sessionId,
                sessionEventType = "session_ended",
                userId = CastledSharedStore.getUserId() ?: "",
                timestamp = DateTimeUtils.getStringFromDate(dateEnded),
                duration = sessionDuration,
                properties = deviceId
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
            Date((currentStartTime).toLong() * 1000)
        val event = CastledSessionEvent(
            sessionId = sessionId,
            sessionEventType = "session_started",
            userId = CastledSharedStore.getUserId() ?: "",
            firstSession = isFirstSession,
            timestamp = DateTimeUtils.getStringFromDate(dateStarted),
            properties = deviceId
        )
        sessionDetails.add(event)
        sessionsRepository.reportSessionEvent(CastledSessionRequest(sessionDetails))
    }

    private fun resetTheValuesForNewSession() {
        CastledSharedStore.setValue(PrefStoreKeys.SESSION_ID, sessionId)
        CastledSharedStore.setValue(PrefStoreKeys.SESSION_DURATION, sessionDuration)
        CastledSharedStore.setValue(PrefStoreKeys.SESSION_START_TIME, sessionStartTime)
        CastledSharedStore.setValue(PrefStoreKeys.SESSION_END_TIME, sessionEndTime)
        CastledSharedStore.setValue(PrefStoreKeys.SESSION_ID, sessionId)
    }

    fun didEnterForeground() {
        CastledSharedStore.getUserId()?.let {
            startCastledSession()
        }
    }

    fun didEnterBackground() {
        CastledSharedStore.getUserId()?.let {
            sessionEndTime = System.currentTimeMillis() / 1000
            sessionDuration += sessionEndTime - currentStartTime
            CastledSharedStore.setValue(PrefStoreKeys.SESSION_DURATION, sessionDuration)
            CastledSharedStore.setValue(PrefStoreKeys.SESSION_END_TIME, sessionEndTime)
        }
    }

    private fun initializeSessionDetails() {
        sessionId = CastledSharedStore.getValue(PrefStoreKeys.SESSION_ID, "")
        isFirstSession = CastledSharedStore.getValue(PrefStoreKeys.SESSION_IS_FIRST, true)
        sessionStartTime = CastledSharedStore.getValue(PrefStoreKeys.SESSION_START_TIME, 0L)
        sessionDuration = CastledSharedStore.getValue(PrefStoreKeys.SESSION_DURATION, 0L)
        sessionEndTime = CastledSharedStore.getValue(PrefStoreKeys.SESSION_END_TIME, 0L)
    }

    override fun onStoreInitialized(context: Context) {
        CastledSharedStore.getUserId()?.let {
            startCastledSession()
        }
    }

    override fun onStoreUserIdSet(context: Context) {
        startCastledSession()
    }

    private fun getCastledSessionId() = UUID.randomUUID().toString()
}
