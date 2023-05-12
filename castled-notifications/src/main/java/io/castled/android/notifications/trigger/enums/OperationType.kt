package io.castled.android.notifications.trigger.enums

@kotlinx.serialization.Serializable
enum class OperationType {
    EQ, GT, LT, GTE, LTE, NEQ, BETWEEN, CONTAINS, NOT_CONTAINS
}