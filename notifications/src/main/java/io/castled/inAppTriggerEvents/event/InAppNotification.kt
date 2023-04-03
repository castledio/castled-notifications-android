package io.castled.inAppTriggerEvents.event

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import androidx.lifecycle.ProcessLifecycleOwner
import io.castled.inAppTriggerEvents.InAppChannelConfig
import io.castled.inAppTriggerEvents.database.PreferencesManager
import io.castled.inAppTriggerEvents.trigger.TriggerEvent
import io.castled.inAppTriggerEvents.observer.AppActivityLifecycleObserver
import io.castled.inAppTriggerEvents.observer.AppLifecycleObserver
import io.castled.inAppTriggerEvents.observer.FragmentLifeCycleObserver
import io.castled.inAppTriggerEvents.requests.connectivity.base.ConnectivityProvider
import io.castled.notifications.logger.CastledLogger
import io.castled.notifications.logger.LogTags
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

object InAppNotification {
    private lateinit var connectivityProvider: ConnectivityProvider

    private val logger : CastledLogger = CastledLogger.getInstance(LogTags.IN_APP)
    private lateinit var inAppConfig : InAppChannelConfig
    internal lateinit var apiKey: String
    private lateinit var application: Application

    internal var hasInternet = false
    private var jobToGetEvents: Job? = null

    internal var userId: String = ""
        get() {
            return field.ifEmpty {
                PreferencesManager(application).userId
            }
        }
        set(value) {
            PreferencesManager(application).userId = value
            field = value
        }

    // TODO: close gitHub-> implement watching for network state changes and retrying network requests #15
    private val connectivityStateListener: ConnectivityProvider.ConnectivityStateListener = object: ConnectivityProvider.ConnectivityStateListener{
        override fun onStateChange(state: ConnectivityProvider.NetworkState) {
            hasInternet = state.hasInternet()
        }
    }

    private fun ConnectivityProvider.NetworkState.hasInternet(): Boolean {
        return (this as? ConnectivityProvider.NetworkState.ConnectedState)?.hasInternet == true
    }

    internal fun isInited() : Boolean = this::apiKey.isInitialized

    internal fun initialize(application: Application, apiKey: String, inAppChannelConfig: InAppChannelConfig) {
        if (this::apiKey.isInitialized) {
            logger.error("Module already initialized!")
            return
        }
        if (!inAppChannelConfig.enable) {
            logger.info("InApp disabled")
            return
        }
        application.registerActivityLifecycleCallbacks(AppActivityLifecycleObserver())
        this.application = application
        observeAppLifecycle(application)
        checkAndStartJobToGetEvents()

        this.apiKey = apiKey
        this.inAppConfig = inAppChannelConfig
    }

    internal fun checkAndStartJobToGetEvents() {
        if ((jobToGetEvents == null || !jobToGetEvents!!.isActive)
            && userId.isNotBlank()
            && this::application.isInitialized)
            startJobToGetEvents()
    }

    private fun startJobToGetEvents() {
        jobToGetEvents = GlobalScope.launch {
            do {
                TriggerEvent.getInstance().fetchAndSaveTriggerEvents(application)
                delay(TimeUnit.SECONDS.toMillis(inAppConfig.fetchFromCloudIntervalSec))
            } while (true)
        }
    }

    private fun observeAppLifecycle(context: Context) {
        connectivityProvider = ConnectivityProvider.createProvider(context)
        val appLifecycleObserver = AppLifecycleObserver(context)
        appLifecycleObserver.provider = connectivityProvider
        appLifecycleObserver.connectivityStateListener = this.connectivityStateListener
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
    }

    internal fun observeLifecycle(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.registerActivityLifecycleCallbacks(AppActivityLifecycleObserver())
        }
    }

    private fun observeLifecycle(fragment: androidx.fragment.app.Fragment, screenName: String) {
        fragment.viewLifecycleOwner.lifecycle.addObserver(
            FragmentLifeCycleObserver(
                fragment.requireContext(),
                screenName
            )
        )
    }

    internal fun logAppEvent(context: Context, eventName: String, eventParams: Map<String, Any>?) {
        val e = mutableMapOf<String, Any?>()
        e["event"] = eventName
        e["params"] = eventParams
        TriggerEvent.getInstance().findAndLaunchEvent(context, e) { events ->
            logger.debug("=>>(Size: ${events.size})")
        }
    }
}