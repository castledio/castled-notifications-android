package io.castled.inAppTriggerEvents.database

import androidx.lifecycle.LiveData
import io.castled.inAppTriggerEvents.models.TriggerEventModel

internal class TriggerEventDatabaseHelperImpl(private val triggerEventsDatabase: TriggerEventsDatabase) :
    TriggerEventDatabaseHelper {

    override suspend fun getTriggerEventsFromDb(): List<TriggerEventModel> =
        triggerEventsDatabase.triggerEventsDao().dbGetTriggerEvents()

    override suspend fun getLiveDataTriggerEventsFromDb(): LiveData<List<TriggerEventModel>> =
        triggerEventsDatabase.triggerEventsDao().dbGetLiveDataTriggerEvents()


    override suspend fun insertTriggerEventsIntoDb(triggerEvents: List<TriggerEventModel>): LongArray =
        triggerEventsDatabase.triggerEventsDao().dbInsertTriggerEvents(triggerEvents)

    override suspend fun deleteDbTriggerEvents(): Int =
        triggerEventsDatabase.triggerEventsDao().dbDeleteAllTriggerEvents()
}