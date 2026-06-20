package com.tertiaryinfotech.freefood.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tertiaryinfotech.freefood.model.Coordinate
import com.tertiaryinfotech.freefood.model.FoodListing
import com.tertiaryinfotech.freefood.model.Recurrence

@Entity(tableName = "listings")
data class FoodListingEntity(
    @PrimaryKey val id: String,
    val title: String,
    val details: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val date: Long,
    val startTime: Long,
    val endTime: Long,
    /** File paths to copied photos, newline-separated. */
    val photos: String,
    val recurrence: String,
    val likes: Int,
    val country: String,
    val createdAt: Long,
) {
    fun toModel(): FoodListing = FoodListing(
        id = id,
        title = title,
        details = details,
        locationName = locationName,
        coordinate = Coordinate(latitude, longitude),
        date = date,
        startTime = startTime,
        endTime = endTime,
        photos = if (photos.isBlank()) emptyList() else photos.split("\n"),
        recurrence = Recurrence.fromName(recurrence),
        likes = likes,
        country = country,
        createdAt = createdAt,
    )

    companion object {
        fun from(m: FoodListing): FoodListingEntity = FoodListingEntity(
            id = m.id,
            title = m.title,
            details = m.details,
            locationName = m.locationName,
            latitude = m.coordinate.latitude,
            longitude = m.coordinate.longitude,
            date = m.date,
            startTime = m.startTime,
            endTime = m.endTime,
            photos = m.photos.joinToString("\n"),
            recurrence = m.recurrence.name,
            likes = m.likes,
            country = m.country,
            createdAt = m.createdAt,
        )
    }
}
