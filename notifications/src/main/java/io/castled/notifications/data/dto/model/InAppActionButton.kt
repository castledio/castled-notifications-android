package io.castled.notifications.data.dto.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class InAppActionButton(val label: String,
    val url: String?,
    val clickAction: String,
    val buttonColor: String,
    val fontColor: String,
    val borderColor: String): Parcelable
