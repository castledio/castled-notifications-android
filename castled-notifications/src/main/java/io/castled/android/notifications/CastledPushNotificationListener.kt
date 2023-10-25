package io.castled.android.notifications

import io.castled.android.notifications.push.models.CastledActionContext
import io.castled.android.notifications.push.models.CastledPushMessage

interface CastledPushNotificationListener {

    fun onCastledPushReceived(pushMessage: CastledPushMessage);

    fun onCastledPushClicked(
        pushMessage: CastledPushMessage,
        actionContext: CastledActionContext
    );

    fun onCastledPushDismissed(pushMessage: CastledPushMessage);

}