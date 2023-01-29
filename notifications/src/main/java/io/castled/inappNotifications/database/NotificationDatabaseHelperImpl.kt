package io.castled.inappNotifications.database

import androidx.lifecycle.LiveData
import io.castled.inappNotifications.models.NotificationModel

class NotificationDatabaseHelperImpl(private val notificationDatabase: NotificationDatabase) :
    NotificationDatabaseHelper {

    override suspend fun getNotificationsFromDb(): List<NotificationModel> =
        notificationDatabase.notificationDao().getNotifications()

    override suspend fun getLiveDataNotificationsFromDb(): LiveData<List<NotificationModel>> =
        notificationDatabase.notificationDao().getLiveDataNotifications()


    override suspend fun insertNotificationIntoDb(notifications: List<NotificationModel>): LongArray =
        notificationDatabase.notificationDao().insertNotifications(notifications)
}