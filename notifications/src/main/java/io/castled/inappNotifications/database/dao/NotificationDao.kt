package io.castled.inappNotifications.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.castled.inappNotifications.models.NotificationModel

@Dao
interface NotificationDao {

    @Query("SELECT * FROM notification")
    fun getNotifications(): List<NotificationModel>

    @Query("SELECT * FROM notification")
    fun getLiveDataNotifications(): LiveData<List<NotificationModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationModel>) : LongArray
}