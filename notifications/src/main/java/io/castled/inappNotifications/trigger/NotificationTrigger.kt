package io.castled.inappNotifications.trigger

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.castled.inappNotifications.database.DatabaseBuilder
import io.castled.inappNotifications.database.NotificationDatabase
import io.castled.inappNotifications.database.NotificationDatabaseHelperImpl
import io.castled.inappNotifications.models.NotificationModel
import io.castled.inappNotifications.notificationConsts.NotificationConstants
import io.castled.inappNotifications.requests.ServiceGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response


private const val TAG = "APITest"

class NotificationTrigger {

    companion object {
        private lateinit var notificationTrigger: NotificationTrigger

        @JvmStatic
        fun getInstance(): NotificationTrigger{
            return if (this::notificationTrigger.isInitialized) notificationTrigger else NotificationTrigger()
        }
    }


    fun fetchNotification(context: Context) {
//        Log.d(TAG, "************* fetchNotification START *************\n")

        CoroutineScope(Main).launch {
            val notifications = fetchNotificationFromCloud(context)
//            Log.d(TAG, "************* fetchNotification END *************\n")
            if (notifications.isNotEmpty()) {
                Log.d(TAG, "${notifications.size} notifications fetched from server.[${notifications.map { it.notificationId }}]")

                withContext(Dispatchers.Default) {
                    val db = NotificationDatabaseHelperImpl(DatabaseBuilder.getInstance(context))

                    val noOfRowDeleted = db.deleteDbNotification()
                    Log.d(TAG, "$noOfRowDeleted notifications deleted from database.")

                    val n = db.insertNotificationIntoDb(notifications)
                    Log.d(TAG, "inserted into db: ${n.toList()}")
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

    fun startObservingTriggerNotification(context: Context) {
        CoroutineScope(Main).launch {
            withContext(Dispatchers.Default) {

                val db = NotificationDatabaseHelperImpl(DatabaseBuilder.getInstance(context))
                val dbTriggerNotifications = db.getNotificationsFromDb()

                if (dbTriggerNotifications.isNotEmpty()) {
                    val notification = dbTriggerNotifications.first()
                    withContext(Main){
                        when (TriggerPopup.getTriggerNotificationType(notification)) {
                            NotificationConstants.Companion.NotificationType.MODAL -> {

                                val message: JsonObject = notification.message.asJsonObject
                                val modal: JsonObject = message.getAsJsonObject("modal")
                                val buttons: JsonArray = modal.getAsJsonArray("actionButtons")
                                val buttonPrimary: JsonObject = buttons[0].asJsonObject
                                val buttonSecondary: JsonObject = buttons[1].asJsonObject

                                if (buttonSecondary.has("url")){
                                    Log.d(TAG, "url: ${buttonSecondary.has("url")}")
                                    if (buttonSecondary.get("url") == null){
                                        Log.d(TAG, "url: null")
                                    } else {
                                        Log.d(TAG, "url not null")
                                        if (buttonSecondary.get("url").isJsonNull){
                                            Log.d(TAG, "url not null1")
                                        } else {
                                            Log.d(TAG, "url not null2")
                                        }
                                    }
                                } else Log.d(TAG, "url2: ${buttonSecondary.has("url")}")


                                TriggerPopup.showDialog(
                                    context,
                                    modal.get("screenOverlayColor").asString,
                                    PopupHeader("Summer sale is Back!", "#FFFFFF", 18f, "#E74C3C"),
                                    PopupMessage(
                                        "30% offer on Electronics, Cloths, Sports and other categories.",
                                        "#FFFFFF",
                                        12f,
                                        "#039ADC"
                                    ),
//                    "http://i.imgur.com/DvpvklR.png",
//                    "https://www.pakainfo.com/wp-content/uploads/2021/09/image-url-for-testing.jpg",
                                    if(modal.get("imageUrl").isJsonNull) "" else modal.get("imageUrl").asString,
                                    "https://www.apple.com/",
                                    PopupPrimaryButton(
                                        buttonPrimary.get("label").asString,
                                        buttonPrimary.get("fontColor").asString,
                                        buttonPrimary.get("buttonColor").asString,
                                        buttonPrimary.get("borderColor").asString,
                                        if (buttonPrimary.get("url").isJsonNull) "" else buttonPrimary.get("url").asString
                                    ),
                                    PopupSecondaryButton(
                                        buttonSecondary.get("label").asString,
                                        buttonSecondary.get("fontColor").asString,
                                        buttonSecondary.get("buttonColor").asString,
                                        buttonSecondary.get("borderColor").asString,
                                        if (buttonSecondary.get("url").isJsonNull) "" else buttonSecondary.get("url").asString
                                    )
                                )
                            }
                            NotificationConstants.Companion.NotificationType.SLIDE_UP -> {

                            }
                            NotificationConstants.Companion.NotificationType.FULL_SCREEN -> {

                            }
                            else -> {}
                        }
                    }
                }

            }

        }
    }

    private suspend fun getDbTriggerNotification(context: Context): List<NotificationModel>{
        var dbTriggerNotifications: List<NotificationModel>? = null
        CoroutineScope(Dispatchers.Default).launch {
            val db = NotificationDatabaseHelperImpl(DatabaseBuilder.getInstance(context))
            dbTriggerNotifications = db.getNotificationsFromDb()

            Log.d(TAG, "getDbTriggerNotification: ${dbTriggerNotifications?.map { it.notificationId }}")
        }

        return dbTriggerNotifications ?: emptyList()
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
