package io.castled.android.notifications.inapp

import io.castled.android.notifications.push.models.CastledActionContext

interface CastledInappNotificationListener {

    fun onCastledInappClicked(
        actionContext: CastledActionContext
    )
}