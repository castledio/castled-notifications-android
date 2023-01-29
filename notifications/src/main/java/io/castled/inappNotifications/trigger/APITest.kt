package io.castled.inappNotifications.trigger

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import io.castled.inappNotifications.database.DatabaseBuilder
import io.castled.inappNotifications.database.NotificationDatabase
import io.castled.inappNotifications.database.NotificationDatabaseHelperImpl
import io.castled.inappNotifications.models.NotificationModel
import io.castled.inappNotifications.requests.ServiceGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response


private const val TAG = "APITest"

class APITest {

    fun fetchNotification(context: Context) {
//        Log.d(TAG, "************* fetchNotification START *************\n")

        CoroutineScope(Main).launch {
            val notifications = fetchNotificationFromCloud(context)
//            Log.d(TAG, "************* fetchNotification END *************\n")
            if (notifications.isNotEmpty()) {
                Toast.makeText(
                    context,
                    "${notifications.size} notifications fetched from server.",
                    Toast.LENGTH_LONG
                ).show()

                withContext(Dispatchers.Default) {
                    val db = NotificationDatabaseHelperImpl(DatabaseBuilder.getInstance(context))
                    val n = db.insertNotificationIntoDb(notifications)
                    Log.d(TAG, "inserted into db: ${n?.toList()}")
                }
            }
        }
    }


    private suspend fun fetchNotificationFromCloud(context: Context?): List<NotificationModel> {

        /*return withContext(IO) {
            val notificationResponse = ServiceGenerator.requestApi().makeNotificationQuery("<api-key>", "support-1@castled.io")
            (if (notificationResponse.isSuccessful && notificationResponse.body() != null)
                notificationResponse.body()
            else
                emptyList<NotificationModel>())!!
        }*/

        return withContext(IO) {
            val notificationResponse = ServiceGenerator.requestApi()
                .makeNotificationQuery("<api-key>", "support-1@castled.io")
//            showApiLog(notificationResponse)
            if (notificationResponse.isSuccessful && notificationResponse.body() != null) {
                notificationResponse.body()
            } else {
                context?.let {
                    withContext(Main) {
                        Toast.makeText(
                            context,
                            "Error while getting data.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                emptyList()
            }
        } as List<NotificationModel>
    }


    fun getDatabaseNotification(context: Context, viewLifecycleOwner: LifecycleOwner) {
        CoroutineScope(Dispatchers.Default).launch {
            val db = NotificationDatabaseHelperImpl(DatabaseBuilder.getInstance(context))
            val n = db.getNotificationsFromDb()
            Log.d(TAG, "observeDatabaseNotification: ${n.size} notifications added/replaced.")
            n.forEach {
                Log.d(TAG, "observeDatabaseNotification: ${it.notificationId}")
            }
        }
    }

    private var notificationObserver: LiveData<List<NotificationModel>>? = null
    fun observeDatabaseNotification(context: Context, viewLifecycleOwner: LifecycleOwner) {
        CoroutineScope(Dispatchers.Default).launch {
            if (notificationObserver == null) {
                val db = NotificationDatabaseHelperImpl(DatabaseBuilder.getInstance(context))
                notificationObserver = db.getLiveDataNotificationsFromDb()
            }

            if (notificationObserver?.hasObservers() != null){
                val db = NotificationDatabaseHelperImpl(DatabaseBuilder.getInstance(context))
                Log.d(TAG, "hasActiveObservers: ${db.getLiveDataNotificationsFromDb().hasActiveObservers()}")
                Log.d(TAG, "hasObservers: ${db.getLiveDataNotificationsFromDb().hasObservers()}")
            }

            if (notificationObserver?.hasObservers() == null || !notificationObserver!!.hasObservers()) {
                withContext(Main) {
                    val db = NotificationDatabaseHelperImpl(DatabaseBuilder.getInstance(context))
                    db.getLiveDataNotificationsFromDb().observe(viewLifecycleOwner) {
                        Log.d(TAG, "observeDatabaseNotification: ${it?.size} notifications added/replaced.")
                        it.forEach {
                            Log.d(TAG, "observeDatabaseNotification: ${it.notificationId}")
                        }
                    }
                }
            }
        }
    }

    private fun showApiLog(notificationResponse: Response<List<NotificationModel>>) {
        Log.d(TAG, "************* fetchNotification FETCHED *************\n")
        Log.d(TAG, "1. isSuccessful: ${notificationResponse.isSuccessful}")
        Log.d(TAG, "2. Body: ${notificationResponse.body()}")
        Log.d(TAG, "3. Code: ${notificationResponse.code()}")
        Log.d(TAG, "4. Message: ${notificationResponse.message()}")
        Log.d(TAG, "5. Headers: ${notificationResponse.headers()}")
        Log.d(TAG, "6. Raw: ${notificationResponse.raw()}")
        Log.d(TAG, "7. ${notificationResponse.body()?.size} ")
        Log.d(TAG, "************* fetchNotification FETCHED DONE *************\n")
    }
}
