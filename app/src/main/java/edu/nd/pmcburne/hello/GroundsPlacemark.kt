package edu.nd.pmcburne.hello

import com.google.gson.annotations.SerializedName

data class GroundsPlacemark(
    val id: Int,
    val name: String,
    val description: String,
    @SerializedName("tag_list")
    val tagList: List<String>,
    @SerializedName("visual_center")
    val visualCenter: VisualCenterDto
)

data class VisualCenterDto(
    val latitude: Double,
    val longitude: Double
)

fun GroundsPlacemark.toEntity(): GroundsLocationEntity {
    return GroundsLocationEntity(
        locationID = id,
        locationName = name,
        locationDetails = description,
        locationTags = tagList,
        locationLat = visualCenter.latitude,
        locationLong = visualCenter.longitude
    )
}