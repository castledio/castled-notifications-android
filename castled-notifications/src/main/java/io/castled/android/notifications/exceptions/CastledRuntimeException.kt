package io.castled.android.notifications.exceptions

class CastledRuntimeException : RuntimeException {
    constructor(message: String?) : super(message)
    constructor(cause: Throwable?) : super(cause)
}