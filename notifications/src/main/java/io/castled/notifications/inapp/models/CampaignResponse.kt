package io.castled.notifications.inapp.models

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

internal data class CampaignResponse(
    @SerializedName("notificationId")
    val notificationId: Int,

    @SerializedName("teamId")
    val teamId: Long,

    @SerializedName("sourceContext")
    val sourceContext: String,

    @SerializedName("startTs")
    val startTs: Long,

    @SerializedName("endTs")
    val endTs: Long,

    @SerializedName("ttl")
    val ttl: Int,

    @SerializedName("displayConfig")
    val displayConfig: CampaignDisplayConfig,

    @SerializedName("trigger")
    val trigger: JsonObject,

    @SerializedName("message")
    val message: JsonObject
)

internal data class CampaignDisplayConfig(
    @SerializedName("displayLimit")
    val displayLimit: Long,

    @SerializedName("minIntervalBtwDisplays")
    val minIntervalBtwDisplays: Long,


    @SerializedName("minIntervalBtwDisplaysGlobal")
    val minIntervalBtwDisplaysGlobal: Long,

    @SerializedName("autoDismissInterval")
    val autoDismissInterval: Long,
)
