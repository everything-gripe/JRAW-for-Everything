package net.dean.jraw.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MediaMetadataPreview(
    @Json(name = "y") val height: Int,
    @Json(name = "x") val width: Int,
    @Json(name = "u") val imgUrl: String?,
    @Json(name = "gif") val gifUrl: String?,
    @Json(name = "mp4") val mp4Url: String?
)

@JsonClass(generateAdapter = true)
data class MediaMetadataItem(
    val id: String?,
    @Json(name = "e") val kind: String?,
    @Json(name = "m") val mime: String?,
    @Json(name = "p") val previews: List<MediaMetadataPreview>?,
    @Json(name = "s") val full: MediaMetadataPreview?
)
