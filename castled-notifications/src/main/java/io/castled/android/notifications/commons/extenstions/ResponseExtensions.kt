package io.castled.android.notifications.commons.extenstions

import retrofit2.Response

fun <T> Response<T>.isSuccessfulOrIgnoredError(): Boolean {
    return this.isSuccessful || (this.code() in 400..499)
}
