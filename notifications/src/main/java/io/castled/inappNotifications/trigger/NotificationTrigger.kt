package io.castled.inappNotifications.trigger

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.castled.inappNotifications.database.DatabaseBuilder
import io.castled.inappNotifications.database.NotificationDatabaseHelperImpl
import io.castled.inappNotifications.models.NotificationModel
import io.castled.inappNotifications.notificationConsts.NotificationConstants
import io.castled.inappNotifications.requests.ServiceGenerator
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import retrofit2.Response

private const val TAG = "NotificationTrigger"

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
       CoroutineScope(Main).launch { fetchNotificationFromCloud(context) }.invokeOnCompletion { getNotificationFromDb(context) }
    }

    private fun getNotificationFromDb(context: Context) = CoroutineScope(Main).launch {
        withContext(Dispatchers.Default) {

            val db = NotificationDatabaseHelperImpl(DatabaseBuilder.getInstance(context))
            val dbTriggerNotifications = db.getNotificationsFromDb()

            if (dbTriggerNotifications.isNotEmpty()) {
                val notification = dbTriggerNotifications.first()
                withContext(Main){
                    when (TriggerPopupDialog.getTriggerNotificationType(notification)) {
                        NotificationConstants.Companion.NotificationType.MODAL -> {
                            getModalTriggerNotification(context, notification)
                        }
                        NotificationConstants.Companion.NotificationType.SLIDE_UP -> {
                            getSlideUpTriggerNotification(context, notification)
                        }
                        NotificationConstants.Companion.NotificationType.FULL_SCREEN -> {
                            getFullScreenTriggerNotification(context, notification)
                        }
                        else -> {}
                    }
                }
            }

        }
    }

    private fun getDefaultNotification(): NotificationModel{
        val fs = JsonObject()
        fs.addProperty("imageUrl", "https://cdn.castled.io/logo/castled_multi_color_logo_only.png")
        fs.addProperty("defaultClickAction", "NONE")
        fs.addProperty("screenOverlayColor", "#f8ffbd")
        fs.addProperty("title", "Full screen title text.")
        fs.addProperty("titleFontColor", "#FFFFFF")
        fs.addProperty("titleFontSize", 18)
        fs.addProperty("titleBgColor", "#E74C3C")
        fs.addProperty("body", "Full screen message text.")
        fs.addProperty("bodyFontColor", "#FFFFFF")
        fs.addProperty("bodyFontSize", 12)
        fs.addProperty("bodyBgColor", "#039ADC")

        val modal = JsonObject()
        modal.addProperty("imageUrl", "https://cdn.castled.io/logo/castled_multi_color_logo_only.png")
        modal.addProperty("defaultClickAction", "NONE")
        modal.addProperty("screenOverlayColor", "#f8ffbd")
        modal.addProperty("title", "Summer sale is Back!")
        modal.addProperty("titleFontColor", "#FFFFFF")
        modal.addProperty("titleFontSize", 18)
        modal.addProperty("titleBgColor", "#E74C3C")
        modal.addProperty("body", "Full Screen \n" +
                "30% offer on Electronics, Cloths, Sports and other categories.")
        modal.addProperty("bodyFontColor", "#FFFFFF")
        modal.addProperty("bodyFontSize", 12)
        modal.addProperty("bodyBgColor", "#039ADC")

        val su = JsonObject()
        su.addProperty("imageUrl", "https://cdn.castled.io/logo/castled_multi_color_logo_only.png")
        su.addProperty("defaultClickAction", "NONE")
        su.addProperty("screenOverlayColor", "#ff99ff")
        su.addProperty("titleBgColor", "#E74C3C")
        su.addProperty("body", "Slide Up \n" +
                "30% offer on Electronics, Cloths, Sports and other categories.")
        su.addProperty("bodyFontColor", "#FFFFFF")
        su.addProperty("bodyFontSize", 12)
        su.addProperty("bodyBgColor", "#039ADC")

        val jsonPrimaryButton = JsonObject()
        jsonPrimaryButton.addProperty("label", "Skip Now")
        jsonPrimaryButton.addProperty("url", "app://a.b")
        jsonPrimaryButton.addProperty("clickAction", "DEEP_LINKING")
        jsonPrimaryButton.addProperty("buttonColor", "#ffffff")
        jsonPrimaryButton.addProperty("fontColor", "#000000")
        jsonPrimaryButton.addProperty("borderColor", "#000000")

        val jsonSecondaryButton = JsonObject()
        jsonSecondaryButton.addProperty("label", "Start Shopping")
        jsonSecondaryButton.addProperty("url", "app://a.b")
        jsonSecondaryButton.addProperty("clickAction", "DISMISS_NOTIFICATION")
        jsonSecondaryButton.addProperty("buttonColor", "#FF6D07")
        jsonSecondaryButton.addProperty("fontColor", "#ffe0da")
        jsonSecondaryButton.addProperty("borderColor", "#5cdb5c")

        val buttons = JsonArray()
        buttons.add(jsonPrimaryButton as JsonElement)
        buttons.add(jsonSecondaryButton as JsonElement)

        fs.add("actionButtons", buttons)
        modal.add("actionButtons", buttons)

        val message = JsonObject()
        message.add("fs", fs)
        message.add("modal", modal)
        message.add("su", su)

        val trigger: JsonObject = JsonObject()

        return  NotificationModel(1, 1, 0, 0L, 0L, 0, trigger, message)
    }

    private fun preparePopupHeader(modal: JsonObject) = PopupHeader(
        if(modal.get("title").isJsonNull) "" else modal.get("title").asString,
        modal.get("titleFontColor").asString,
        modal.get("titleFontSize").asFloat,
        modal.get("titleBgColor").asString
    )

    private fun preparePopupMessage(modal: JsonObject) = PopupMessage(
        if (modal.get("body").isJsonNull) "" else modal.get("body").asString,
        modal.get("bodyFontColor").asString,
        modal.get("bodyFontSize").asFloat,
        modal.get("bodyBgColor").asString
    )

    private fun preparePopupPrimaryButton(primaryPopupButtonJson: JsonObject) = PopupPrimaryButton(
        primaryPopupButtonJson.get("label").asString,
        primaryPopupButtonJson.get("fontColor").asString,
        primaryPopupButtonJson.get("buttonColor").asString,
        primaryPopupButtonJson.get("borderColor").asString,
        if (primaryPopupButtonJson.get("url").isJsonNull) "" else primaryPopupButtonJson.get("url").asString
    )

    private fun preparePopupSecondaryButton(secondaryPopupButtonJson: JsonObject) = PopupSecondaryButton(
        secondaryPopupButtonJson.get("label").asString,
        secondaryPopupButtonJson.get("fontColor").asString,
        secondaryPopupButtonJson.get("buttonColor").asString,
        secondaryPopupButtonJson.get("borderColor").asString,
        if (secondaryPopupButtonJson.get("url").isJsonNull) "" else secondaryPopupButtonJson.get("url").asString
    )

    private fun getModalTriggerNotification(context: Context, notification: NotificationModel) {
        val message: JsonObject = notification.message.asJsonObject
        val modal: JsonObject = message.getAsJsonObject("modal")
        val buttons: JsonArray = modal.getAsJsonArray("actionButtons")
        val buttonPrimary: JsonObject = buttons[0].asJsonObject
        val buttonSecondary: JsonObject = buttons[1].asJsonObject

        TriggerPopupDialog.showDialog(
            context,
            modal.get("screenOverlayColor").asString,
            preparePopupHeader(modal),
            preparePopupMessage(modal),
            if(modal.get("imageUrl").isJsonNull) "" else modal.get("imageUrl").asString,
            if(modal.get("defaultClickAction").isJsonNull) "" else modal.get("defaultClickAction").asString,
            preparePopupPrimaryButton(buttonPrimary),
            preparePopupSecondaryButton(buttonSecondary)
        )
    }

    private fun getFullScreenTriggerNotification(context: Context, notification: NotificationModel) {
        val message: JsonObject = notification.message.asJsonObject
        val modal: JsonObject = message.getAsJsonObject("fs")
        val buttons: JsonArray = modal.getAsJsonArray("actionButtons")
        val buttonPrimary: JsonObject = buttons[0].asJsonObject
        val buttonSecondary: JsonObject = buttons[1].asJsonObject

        TriggerPopupDialog.showFullscreenDialog(
            context,
            modal.get("screenOverlayColor").asString,
            preparePopupHeader(modal),
            preparePopupMessage(modal),
            if(modal.get("imageUrl").isJsonNull) "" else modal.get("imageUrl").asString,
            if(modal.get("defaultClickAction").isJsonNull) "" else modal.get("defaultClickAction").asString,
            preparePopupPrimaryButton(buttonPrimary),
            preparePopupSecondaryButton(buttonSecondary)
        )
    }

    private fun getSlideUpTriggerNotification(context: Context, notification: NotificationModel) {
        Log.d(TAG, "notification: $notification")
        val message: JsonObject = notification.message.asJsonObject
        val modal: JsonObject =
            if (message.has("su"))
                message.getAsJsonObject("su")
            else
                getDefaultNotification().message.getAsJsonObject("su")

        Log.d(TAG, "modal: $modal")
        TriggerPopupDialog.showSlideUpDialog(
            context,
            modal.get("screenOverlayColor").asString,
            preparePopupMessage(modal),
            if(modal.get("imageUrl").isJsonNull) "" else modal.get("imageUrl").asString,
            if(modal.get("defaultClickAction").isJsonNull) "" else modal.get("defaultClickAction").asString
        )
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
