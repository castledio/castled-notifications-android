package io.castled.notifications.data.dto.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class InAppModel(val type: String,
    val imageUrl: String?,
    val defaultClickAction: String,
    val url: String?,
    val title: String,
    val body: String,
    val fontColor: String,
    val fontSize: Int,
    val bgColor: String,
    val screenOverlayColor: String,
    val actionButtons: List<InAppActionButton> = listOf()): Parcelable
