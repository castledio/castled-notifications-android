package io.castled.android.notifications.inbox.viewmodel

import android.content.Context
import io.castled.android.notifications.network.CastledRetrofitClient
import io.castled.android.notifications.inapp.models.CampaignResponse
import io.castled.android.notifications.inbox.model.InboxResponse
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.store.CastledDbBuilder
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.store.models.AppInbox
import io.castled.android.notifications.store.models.Campaign
import io.castled.android.notifications.workmanager.CastledNetworkWorkManager
import io.castled.android.notifications.workmanager.models.CastledInAppEventRequest
import retrofit2.Response
import java.io.IOException

internal class InboxRepository(context: Context) {

    private val campaignDao = CastledDbBuilder.getDbInstance(context).inboxDao()
    private val logger = CastledLogger.getInstance(LogTags.INBOX_REPOSITORY)
    private val inAppApi = CastledRetrofitClient.create(InboxApi::class.java)
    private val networkWorkManager = CastledNetworkWorkManager.getInstance(context)

    suspend fun getInbox(): List<AppInbox> {
        return campaignDao.dbGetInbox()
    }

    suspend fun insertInboxIntoDb(inboxItems: List<AppInbox>): LongArray {
        return campaignDao.dbInsertInbox(inboxItems)
    }

    suspend fun deleteDbInbox(inboxItems: List<AppInbox>): Int {
        return campaignDao.dbDeleteAllInboxItems(inboxItems)
    }



    suspend fun fetchLiveInbox(): List<InboxResponse>? {
        try {
            val response = inAppApi.fetchInboxItems(
                CastledSharedStore.getApiKey(),
                CastledSharedStore.getUserId()
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

//    suspend fun reportEvent(request: CastledInAppEventRequest) {
//        networkWorkManager.apiCallWithRetry(
//            request = request,
//            apiCall = {
//                return@apiCallWithRetry inAppApi.reportEvent(
//                    CastledSharedStore.getApiKey(),
//                    it as CastledInAppEventRequest
//                )
//            }
//        )
//    }
//
//    suspend fun reportEventNoRetry(request: CastledInAppEventRequest): Response<Void?> {
//        return inAppApi.reportEvent(CastledSharedStore.getApiKey(), request)
//    }

}