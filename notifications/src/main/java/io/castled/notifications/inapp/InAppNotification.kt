package io.castled.notifications.inapp

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import androidx.lifecycle.ProcessLifecycleOwner
import io.castled.notifications.inapp.observer.AppActivityLifecycleObserver
import io.castled.notifications.inapp.observer.AppLifecycleObserver
import io.castled.notifications.inapp.observer.FragmentLifeCycleObserver
import io.castled.notifications.connectivity.base.ConnectivityProvider
import io.castled.notifications.logger.CastledLogger
import io.castled.notifications.logger.LogTags
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import java.util.concurrent.TimeUnit

internal object InAppNotification {
    private lateinit var connectivityProvider: ConnectivityProvider

    private val logger: CastledLogger = CastledLogger.getInstance(LogTags.IN_APP)
    private lateinit var externalScope: CoroutineScope
    private lateinit var inAppConfig: InAppChannelConfig
    private lateinit var inAppController: InAppController
    private var enabled = false
    private var fetchJob: Job? = null

    internal var hasInternet = false
    private var jobToGetEvents: Job? = null

    // TODO: close gitHub-> implement watching for network state changes and retrying network requests #15
    private val connectivityStateListener: ConnectivityProvider.ConnectivityStateListener =
        object : ConnectivityProvider.ConnectivityStateListener {
            override fun onStateChange(state: ConnectivityProvider.NetworkState) {
                hasInternet = state.hasInternet()
            }
        }

    private fun ConnectivityProvider.NetworkState.hasInternet(): Boolean {
        return (this as? ConnectivityProvider.NetworkState.ConnectedState)?.hasInternet == true
    }

    internal fun init(
        application: Application,
        externalScope: CoroutineScope,
        inAppChannelConfig: InAppChannelConfig
    ) {
        if (!inAppChannelConfig.enable) {
            logger.info("InApp disabled")
            return
        }
        this.externalScope = externalScope
        this.enabled = true
        this.inAppController = InAppController(application)
        application.registerActivityLifecycleCallbacks(AppActivityLifecycleObserver())
        observeAppLifecycle(application)
        inAppConfig = inAppChannelConfig
    }

    internal fun startCampaignJob() {
        if (fetchJob == null || !fetchJob!!.isActive) {
            externalScope.launch(Default) {
                do {
                    inAppController.fetchAndSaveLiveCampaigns()
                    delay(TimeUnit.SECONDS.toMillis(inAppConfig.fetchFromCloudIntervalSec))
                } while (true)
            }
        }
    }

    private fun observeAppLifecycle(application: Application) {
        connectivityProvider = ConnectivityProvider.createProvider(application)
        val appLifecycleObserver = AppLifecycleObserver(application)
        appLifecycleObserver.provider = connectivityProvider
        appLifecycleObserver.connectivityStateListener = connectivityStateListener
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

    internal suspend fun logAppEvent(
        context: Context,
        eventName: String,
        eventParams: Map<String, Any>?
    ) {
        if (!enabled) {
            logger.debug("Ignoring app event. In-app disabled")
        }
        val e = mutableMapOf<String, Any?>()
        e["event"] = eventName
        e["params"] = eventParams
        inAppController.findAndLaunchInApp(context, e)
    }
}