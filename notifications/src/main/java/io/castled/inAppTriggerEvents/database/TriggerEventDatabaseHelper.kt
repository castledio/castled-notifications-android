package io.castled.inAppTriggerEvents.database

import androidx.lifecycle.LiveData
import io.castled.inAppTriggerEvents.models.TriggerEventModel

internal interface TriggerEventDatabaseHelper {

    suspend fun getTriggerEventsFromDb(): List<TriggerEventModel>

    suspend fun getLiveDataTriggerEventsFromDb(): LiveData<List<TriggerEventModel>>

    suspend fun insertTriggerEventsIntoDb(triggerEvents: List<TriggerEventModel>): LongArray

    suspend fun deleteDbTriggerEvents(): Int

}