package io.castled.android.notifications.inbox.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import io.castled.android.notifications.inbox.InboxEventUtils
import io.castled.android.notifications.inbox.model.InboxResponse
import io.castled.android.notifications.inbox.model.InboxResponseConverter.toInbox
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.network.CastledRetrofitClient
import io.castled.android.notifications.store.CastledDbBuilder
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.store.models.AppInbox
import io.castled.android.notifications.workmanager.CastledNetworkWorkManager
import io.castled.android.notifications.workmanager.models.CastledInboxEventRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

internal class InboxRepository(context: Context) {

    private val inboxDao = CastledDbBuilder.getDbInstance(context).inboxDao()
    private val logger = CastledLogger.getInstance(LogTags.INBOX_REPOSITORY)
    private val inboxApi = CastledRetrofitClient.create(InboxApi::class.java)
    private val networkWorkManager = CastledNetworkWorkManager.getInstance(context)
    internal val cachedInboxItems = mutableListOf<AppInbox>()
    internal suspend fun refreshInbox() {
        val liveInboxResponse = fetchLiveInbox() ?: return
        val liveInboxItems = liveInboxResponse.map { it.toInbox() }
        val cachedInboxItemsMapById = cachedInboxItems.associateBy { it.messageId }
        val liveInInboxItemsMapById = liveInboxItems.associateBy { it.messageId }
        val expiredInboxItems =
            cachedInboxItems.filterNot { liveInInboxItemsMapById.containsKey(it.messageId) }
        val newInboxItems =
            liveInboxItems.filterNot { cachedInboxItemsMapById.containsKey(it.messageId) }
        insertInboxIntoDb(newInboxItems)
        deleteDbInbox(expiredInboxItems)
    }

    private suspend fun insertInboxIntoDb(inboxItems: List<AppInbox>): LongArray {
        return inboxDao.dbInsertInbox(inboxItems)
    }

    private suspend fun deleteDbInbox(inboxItems: List<AppInbox>): Int {
        return inboxDao.dbDeleteAllInboxItems(inboxItems)
    }

    internal fun deleteInboxItem(inbox: AppInbox) {
        inboxDao.delete(inbox)
    }

    private suspend fun fetchLiveInbox(): List<InboxResponse>? {
        try {
            val response = inboxApi.fetchInboxItems(
                CastledSharedStore.getApiKey(), CastledSharedStore.getUserId()
            )
            return if (response.isSuccessful) {
                response.body()
            } else {
                // Handle API errors (e.g., 4xx or 5xx status codes)
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                logger.error(errorMessage)
                null
            }
        } catch (e: IOException) {
            logger.error("Network error!", e)
        } catch (e: Exception) {
            logger.error("Unknown error!", e)
        }
        return null
    }

    internal fun changeTheStatusToread(inboxItems: Set<AppInbox>) {

        GlobalScope.launch(Dispatchers.IO) {
            try {
                inboxItems.forEach {
                    it.isRead = true
                    inboxDao.updateInboxItem(it)
                }
            } catch (e: Exception) {
                // Handle any exceptions that may occur during database operations
                e.printStackTrace()
                // You can also log the exception or show an error message to the user
            }


        }
    }

    internal suspend fun deleteInboxItem(
        inbox: AppInbox, completion: (Boolean, String) -> Unit
    ) {
        try {
            val response = inboxApi.reportInboxEvent(
                CastledSharedStore.getApiKey(), InboxEventUtils.getInboxEventRequest(
                    inbox, "", "DELETED"
                )
            )
            if (response.isSuccessful) {
                completion(true, "")
                return
            } else {
                // Handle API errors (e.g., 4xx or 5xx status codes)
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                logger.error(errorMessage)
                completion(false, errorMessage)
                return
            }
        } catch (e: IOException) {
            logger.error("Network error!", e)
            completion(false, e.localizedMessage)

        } catch (e: Exception) {
            logger.error("Unknown error!", e)
            completion(false, e.localizedMessage)
        }

    }

    internal fun observeMovieLiveData(): LiveData<List<AppInbox>> {
        return inboxDao.getInboxItems()
    }

    suspend fun reportEvent(request: CastledInboxEventRequest) {
        networkWorkManager.apiCallWithRetry(request = request, apiCall = {
            return@apiCallWithRetry inboxApi.reportInboxEvent(
                CastledSharedStore.getApiKey(), it as CastledInboxEventRequest
            )
        })
    }

    suspend fun reportEventNoRetry(request: CastledInboxEventRequest): Response<Void?> {
        return inboxApi.reportInboxEvent(CastledSharedStore.getApiKey(), request)
    }

}