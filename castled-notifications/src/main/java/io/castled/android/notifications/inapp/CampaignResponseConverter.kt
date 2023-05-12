package io.castled.android.notifications.inapp

import io.castled.android.notifications.inapp.models.CampaignResponse
import io.castled.android.notifications.store.models.Campaign

internal object CampaignResponseConverter {

    fun CampaignResponse.toCampaign() : Campaign {
        return Campaign(
            notificationId = this.notificationId,
            teamId = this.teamId,
            sourceContext = this.sourceContext,
            startTs = this.startTs,
            endTs = this.endTs,
            displayConfig = this.displayConfig,
            priority = this.priority,
            trigger = this.trigger,
            message = this.message,
            timesDisplayed = 0,
            lastDisplayedTime = 0,
            ttl = 0,
            expired = false
        )
    }
}