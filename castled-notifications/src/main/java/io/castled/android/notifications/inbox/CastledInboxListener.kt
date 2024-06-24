package io.castled.android.notifications.inbox

import io.castled.android.notifications.push.models.CastledActionContext

interface CastledInboxListener {

    fun onCastledInboxClicked(
        actionContext: CastledActionContext
    )
}