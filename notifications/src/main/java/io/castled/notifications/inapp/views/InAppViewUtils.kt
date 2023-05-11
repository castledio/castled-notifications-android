package io.castled.notifications.inapp.views

import io.castled.notifications.commons.ClickActionParams
import io.castled.notifications.push.models.CastledClickAction
import kotlinx.serialization.json.*

object InAppViewUtils {

    fun getHeaderViewParams(modal: JsonObject) = HeaderViewParams(
        (modal["title"] as JsonPrimitive?)?.content ?: "",
        (modal["titleFontColor"] as JsonPrimitive?)?.content ?: "",
        (modal["titleFontSize"] as JsonPrimitive?)?.float ?: 0F,
        (modal["titleBgColor"] as JsonPrimitive?)?.content ?: ""
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
        val buttons = modal["actionButtons"]?.jsonArray ?: return null
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

    fun getInAppRootActionParams(modal: JsonObject) : ClickActionParams {
        return ClickActionParams(
            actionLabel = null,
            action = CastledClickAction.valueOf(modal["defaultClickAction"]?.jsonPrimitive?.content!!),
            uri = modal["url"]?.jsonPrimitive?.content ?: "",
            keyVals = modal["keyVals"]?.jsonObject?.entries?.associate { (key, value) ->
                key to (value as JsonPrimitive).content
            }
        )
    }

    fun getPrimaryButtonActionParams(modal: JsonObject): ClickActionParams? {
        val btnViewParams =  getPrimaryButtonViewParams(modal)
        val buttons = modal["actionButtons"]?.jsonArray ?: return null
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

    fun getSecondaryButtonViewParams(modal: JsonObject): ButtonViewParams? {
        val buttons = modal["actionButtons"]?.jsonArray
        if (buttons.isNullOrEmpty() || buttons.size < 2) {
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
}