package io.castled.android.notifications.inapp.views

import io.castled.android.notifications.commons.ClickActionParams
import io.castled.android.notifications.push.models.CastledClickAction
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.float
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object InAppViewUtils {

    fun getHeaderViewParams(modal: JsonObject) = HeaderViewParams(
        (modal["title"] as JsonPrimitive?)?.content ?: "",
        (modal["titleFontColor"] as JsonPrimitive?)?.content ?: "",
        (modal["titleFontSize"] as JsonPrimitive?)?.float ?: 0F,
        (modal["titleBgColor"] as JsonPrimitive?)?.content ?: "",
        (modal["screenOverlayColor"] as JsonPrimitive?)?.content ?: ""
    )

    fun getMessageViewParams(modal: JsonObject): MessageViewParams {
        if (modal["bodyFontColor"] != null && modal["bodyFontSize"] != null && modal["bodyBgColor"] != null) {
            return MessageViewParams(
                (modal["body"] as JsonPrimitive).content,
                (modal["bodyFontColor"] as JsonPrimitive).content,
                (modal["bodyFontSize"] as JsonPrimitive).float,
                (modal["bodyBgColor"] as JsonPrimitive).content
            )
        } else if (modal["bgColor"] != null && modal["fontSize"] != null && modal["fontColor"] != null) {
            return MessageViewParams(
                (modal["body"] as JsonPrimitive).content,
                (modal["fontColor"] as JsonPrimitive).content,
                (modal["fontSize"] as JsonPrimitive).float,
                (modal["bgColor"] as JsonPrimitive).content
            )
        } else return MessageViewParams(
            (modal["body"] as JsonPrimitive?)?.content ?: "",
            "#000000",
            18F,
            "#FFFFFF"
        )
    }

    fun getPrimaryButtonViewParams(modal: JsonObject): ButtonViewParams? {
        val buttons = modal["actionButtons"]?.jsonArray
        if (buttons.isNullOrEmpty() || buttons.size < 2) {
            return null
        }
        val primaryButtonJson = buttons.last().jsonObject
        val keyVals = primaryButtonJson["keyVals"]?.jsonObject
        return ButtonViewParams(
            primaryButtonJson["label"]?.jsonPrimitive?.content!!,
            primaryButtonJson["fontColor"]?.jsonPrimitive?.content!!,
            primaryButtonJson["buttonColor"]?.jsonPrimitive?.content!!,
            primaryButtonJson["borderColor"]?.jsonPrimitive?.content!!,
            CastledClickAction.valueOf(primaryButtonJson["clickAction"]?.jsonPrimitive?.content!!),
            primaryButtonJson["url"]?.jsonPrimitive?.content ?: "",
            keyVals?.entries?.associate { (key, value) ->
                key to (value as JsonPrimitive).content
            }
        )
    }

    fun getInAppRootActionParams(modal: JsonObject): ClickActionParams {
        return ClickActionParams(
            actionLabel = null,
            action = CastledClickAction.valueOf(
                modal["defaultClickAction"]?.jsonPrimitive?.content
                    ?: modal["clickAction"]?.jsonPrimitive?.content // for banner body click
                    ?: CastledClickAction.NONE.toString()
            ),
            uri = modal["url"]?.jsonPrimitive?.content ?: "",
            keyVals = (modal["keyVals"] as? JsonObject)?.jsonObject?.entries?.associate { (key, value) ->
                key to (value as JsonPrimitive).content
            }
        )
    }

    fun getInAppDismissedActionParams(): ClickActionParams {
        return ClickActionParams(
            actionLabel = null,
            action = CastledClickAction.DISMISS_NOTIFICATION,
            uri = "",
            keyVals = null
        )
    }

    fun getPrimaryButtonActionParams(modal: JsonObject): ClickActionParams? {
        val buttons = modal["actionButtons"]?.jsonArray
        if (buttons.isNullOrEmpty() || buttons.size < 2) {
            return null
        }
        val primaryButtonJson = buttons.last().jsonObject
        val keyVals = primaryButtonJson["keyVals"]?.jsonObject
        return ClickActionParams(
            actionLabel = primaryButtonJson["label"]?.jsonPrimitive?.content!!,
            action = CastledClickAction.valueOf(primaryButtonJson["clickAction"]?.jsonPrimitive?.content!!),
            uri = primaryButtonJson["url"]?.jsonPrimitive?.content ?: "",
            keyVals = keyVals?.entries?.associate { (key, value) ->
                key to (value as JsonPrimitive).content
            }
        )
    }

    fun getWebViewButtonActionParams(modal: JsonObject): ClickActionParams {
        val keyVals = modal["keyVals"]?.jsonObject
        return ClickActionParams(
            actionLabel = modal["keyVals"]?.jsonObject?.get("button_title")?.jsonPrimitive?.content
                ?: (modal.get("clickAction")?.jsonPrimitive?.content ?: ""),
            action = CastledClickAction.valueOf(
                (modal.get("clickAction")?.jsonPrimitive?.content ?: "")
            ),
            uri = modal.get("clickActionUrl")?.jsonPrimitive?.content ?: "",
            keyVals = keyVals?.entries?.associate { (key, value) ->
                key to (value as JsonPrimitive).content
            }
        )
    }

    fun getSecondaryButtonViewParams(modal: JsonObject): ButtonViewParams? {
        val buttons = modal["actionButtons"]?.jsonArray
        if (buttons.isNullOrEmpty() || buttons.size < 1) {
            return null
        }
        val secondaryButtonJson = buttons.first().jsonObject
        val keyVals = secondaryButtonJson["keyVals"]?.jsonObject
        return ButtonViewParams(
            secondaryButtonJson["label"]?.jsonPrimitive?.content!!,
            secondaryButtonJson["fontColor"]?.jsonPrimitive?.content!!,
            secondaryButtonJson["buttonColor"]?.jsonPrimitive?.content!!,
            secondaryButtonJson["borderColor"]?.jsonPrimitive?.content!!,
            CastledClickAction.valueOf(secondaryButtonJson["clickAction"]?.jsonPrimitive?.content!!),
            secondaryButtonJson["url"]?.jsonPrimitive?.content ?: "",
            keyVals?.entries?.associate { (key, value) ->
                key to (value as JsonPrimitive).content
            }
        )
    }

    fun getImageViewParams(modalParams: JsonObject) =
        modalParams["imageUrl"]?.let { ImageViewParams(it.jsonPrimitive.content) }

    fun shouldShowCloseButton(modal: JsonObject): Boolean {
        return try {
            (modal["showCloseButton"] as? JsonPrimitive?)?.content?.toBoolean() ?: true
        } catch (e: Exception) {
            true
        }
    }


}