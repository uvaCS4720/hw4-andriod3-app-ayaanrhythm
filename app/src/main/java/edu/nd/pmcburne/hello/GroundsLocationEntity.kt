package edu.nd.pmcburne.hello

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
data class GroundsLocationEntity(
    @PrimaryKey val locationID: Int,
    val locationName: String,
    val locationDetails: String,
    val locationTags: List<String>,
    val locationLat: Double,
    val locationLong: Double,
)