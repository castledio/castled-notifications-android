package io.castled.android.mipush

import android.content.Context
import com.xiaomi.channel.commonutils.android.Region
import com.xiaomi.mipush.sdk.MiPushClient
import io.castled.android.mipush.consts.MiLogTags
import io.castled.android.notifications.CastledConfigs
import io.castled.android.notifications.CastledNotifications
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.push.models.PushTokenType

internal object MiPushManager {

    private val logger = CastledLogger.getInstance(MiLogTags.MI_PUSH)

    fun register(context: Context) {
        val configs = CastledNotifications.getCastledConfigs()
        val region = when (configs.xiaomiRegion) {
            CastledConfigs.XiaomiRegion.Global -> Region.Global
            CastledConfigs.XiaomiRegion.India -> Region.India
            CastledConfigs.XiaomiRegion.Europe -> Region.Europe
            else -> null
        } ?: run {
            logger.error("Xiaomi region not set!")
            return
        }
        if (configs.xiaomiAppId != null && configs.xiaomiAppKey != null) {
            MiPushClient.setRegion(region)
            MiPushClient.registerPush(context, configs.xiaomiAppId, configs.xiaomiAppKey)
        } else {
            logger.verbose("Xiaomi configs not set")
        }
    }

    fun getToken(context: Context): String? {
        val regId = MiPushClient.getRegId(context)
        val region = MiPushClient.getAppRegion(context)
        return regId?.let { "$region:::$it" }
    }

    fun onNewToken(token: String) {
        CastledNotifications.onTokenFetch(token, PushTokenType.MI_PUSH)
    }

    fun unRegister(context: Context) {
        MiPushClient.unregisterPush(context)
    }

}