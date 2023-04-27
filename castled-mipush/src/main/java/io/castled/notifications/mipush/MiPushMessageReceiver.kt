package io.castled.notifications.mipush

import android.content.Context
import com.xiaomi.mipush.sdk.*
import io.castled.notifications.CastledNotifications
import io.castled.notifications.logger.CastledLogger
import io.castled.notifications.mipush.consts.MiLogTags

class MiPushMessageReceiver : PushMessageReceiver() {

    private val logger = CastledLogger.getInstance(MiLogTags.MI_PUSH)

    override fun onReceivePassThroughMessage(context: Context?, message: MiPushMessage) {
        if (message.isCastledPushMessage()) {
            // Push message from castled server
            logger.verbose("Push message received from Castled")
            val pushMessage = message.toCastledPushMessage() ?: return
            CastledNotifications.handlePushNotification(context!!, pushMessage)
        } else {
            logger.verbose("Push message not from Castled")
        }
    }

    override fun onNotificationMessageClicked(context: Context?, message: MiPushMessage) {
        logger.verbose("onNotificationMessageClicked $message")
    }

    override fun onNotificationMessageArrived(context: Context?, message: MiPushMessage) {
        logger.verbose("onNotificationMessageArrived $message")
    }

    override fun onCommandResult(context: Context?, message: MiPushCommandMessage) {
        logger.verbose("onCommandResult $message")
    }

    override fun onReceiveRegisterResult(context: Context?, message: MiPushCommandMessage) {
        val regId =  message.commandArguments?.firstOrNull() ?: return
        val cmdArg2 = message.commandArguments?.getOrNull(1)
        if (MiPushClient.COMMAND_REGISTER == message.command) {
            if (message.resultCode == ErrorCode.SUCCESS.toLong()) {
                val region = MiPushClient.getAppRegion(context)
                MiPushManager.onNewToken("$region:$regId")
                logger.verbose("onReceiveRegisterResult, token:$regId, region:$region")
            } else {
                logger.debug("onReceiveRegisterResult, error:${message.resultCode} arg1:$regId, arg2:$cmdArg2")
            }
        }
    }

}