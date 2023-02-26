package io.castled.inAppTriggerEvents.database.dao

import androidx.room.Dao
import androidx.room.Insert


@Dao
interface LogEventDao {

    @Insert
    suspend fun insertEventLog()
}