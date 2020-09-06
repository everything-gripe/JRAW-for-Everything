package net.dean.jraw.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GalleryData(
    val items: List<GalleryDataItem>
)

@JsonClass(generateAdapter = true)
data class GalleryDataItem(
    val caption: String?,
    @Json(name = "media_id") val mediaId: String
)
