package io.castled.inAppTriggerEvents.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy


@Dao
interface LogEventDao {

    @Insert(onConflict = OnConflictStrategy.NONE)
    suspend fun insertEventLog()

//    val logEventDate = System.currentTimeMillis()
}