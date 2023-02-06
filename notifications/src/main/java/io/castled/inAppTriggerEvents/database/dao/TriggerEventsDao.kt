package io.castled.inAppTriggerEvents.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.castled.inAppTriggerEvents.models.TriggerEventModel

@Dao
internal interface TriggerEventsDao {

    @Query("SELECT * FROM trigger_event")
    fun dbGetTriggerEvents(): List<TriggerEventModel>

    @Query("SELECT * FROM trigger_event")
    fun dbGetLiveDataTriggerEvents(): LiveData<List<TriggerEventModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun dbInsertTriggerEvents(notifications: List<TriggerEventModel>) : LongArray

    @Query("DELETE FROM trigger_event")
    fun dbDeleteAllTriggerEvents(): Int
}