package io.castled.notifications.commons

import android.os.Bundle

internal object CastledMapUtils {

    fun mapToQueryParams(baseUrl: String, queryParams: Map<String, String>): String {
        val queryString = buildString {
            queryParams.forEach { (key, value) ->
                if (key.isNotBlank() && value.isNotBlank()) {
                    append(if (isEmpty()) "?" else "&")
                    append("${key}=${value}")
                }
            }
        }
        return "$baseUrl$queryString"
    }

    fun mapToBundle(keyValues: Map<String, String>): Bundle {
        val bundle = Bundle()
        keyValues.forEach { (key, value) ->
            bundle.putString(key, value)
        }
        return bundle
    }
}