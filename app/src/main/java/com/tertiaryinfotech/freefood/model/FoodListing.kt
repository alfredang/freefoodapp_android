package com.tertiaryinfotech.freefood.model

import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * How often a free-food giveaway repeats. Lets shops/bakeries/stalls post once for a
 * regular giveaway instead of re-creating a listing every time.
 */
enum class Recurrence(val label: String) {
    NONE("One-time"),
    DAILY("Daily"),
    WEEKLY("Weekly");

    companion object {
        fun fromName(value: String?): Recurrence =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: NONE
    }
}

data class Coordinate(
    val latitude: Double,
    val longitude: Double,
)

/**
 * A free-food listing. Mirrors the iOS `FoodListing` model: dates are kept as epoch
 * milliseconds and the same computed properties (effective date for recurring posts,
 * combined start/end, expiry and active state) are reproduced here.
 */
data class FoodListing(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val details: String,
    val locationName: String,
    val coordinate: Coordinate,
    val date: Long,
    val startTime: Long,
    val endTime: Long,
    val photos: List<String> = emptyList(),
    val recurrence: Recurrence = Recurrence.NONE,
    val likes: Int = 0,
    /** ISO country code (e.g. "SG") of the pickup location, for country-based filtering. */
    val country: String = "",
    val createdAt: Long = System.currentTimeMillis(),
) {
    private val zone: ZoneId get() = ZoneId.systemDefault()

    val expiresAt: Long
        get() = Instant.ofEpochMilli(createdAt).plus(7, ChronoUnit.DAYS).toEpochMilli()

    /** Put the hour/minute of [time] onto the calendar day of [day]. */
    private fun combine(time: Long, day: Long): Long {
        val t = Instant.ofEpochMilli(time).atZone(zone)
        val d = Instant.ofEpochMilli(day).atZone(zone)
        return d.withHour(t.hour).withMinute(t.minute).withSecond(0).withNano(0)
            .toInstant().toEpochMilli()
    }

    /**
     * For recurring listings, the next occurrence whose end time is still in the future;
     * for one-time listings, just [date].
     */
    fun effectiveDate(now: Long = System.currentTimeMillis()): Long {
        if (recurrence == Recurrence.NONE) return date
        var day = Instant.ofEpochMilli(date).atZone(zone)
        var guard = 0
        while (combine(endTime, day.toInstant().toEpochMilli()) < now && guard < 1000) {
            day = if (recurrence == Recurrence.DAILY) day.plusDays(1) else day.plusWeeks(1)
            guard += 1
        }
        return day.toInstant().toEpochMilli()
    }

    fun combinedStartDate(now: Long = System.currentTimeMillis()): Long =
        combine(startTime, effectiveDate(now))

    fun combinedEndDate(now: Long = System.currentTimeMillis()): Long =
        combine(endTime, effectiveDate(now))

    /** A past event: the food's end time has already passed. Recurring posts roll forward. */
    fun hasEnded(now: Long = System.currentTimeMillis()): Boolean = combinedEndDate(now) < now

    /**
     * Recurring listings stay live indefinitely (they keep rolling forward). One-time
     * listings are shown only while upcoming/ongoing and posted within the last 7 days.
     */
    fun isActive(now: Long = System.currentTimeMillis()): Boolean =
        if (recurrence != Recurrence.NONE) true else (!hasEnded(now) && expiresAt > now)

    /** Great-circle distance in metres from [lat]/[lon], or null when no location. */
    fun distanceFrom(lat: Double?, lon: Double?): Double? {
        if (lat == null || lon == null) return null
        val r = 6_371_000.0
        val dLat = Math.toRadians(coordinate.latitude - lat)
        val dLon = Math.toRadians(coordinate.longitude - lon)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat)) * cos(Math.toRadians(coordinate.latitude)) *
            sin(dLon / 2) * sin(dLon / 2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    companion object {
        val sample: FoodListing
            get() {
                val now = System.currentTimeMillis()
                return FoodListing(
                    title = "Pastries after meetup",
                    details = "Mixed croissants and sandwiches left from a morning event. Please bring your own bag.",
                    locationName = "Tanjong Pagar Centre",
                    coordinate = Coordinate(1.2764, 103.8458),
                    date = now,
                    startTime = now,
                    endTime = now + 2 * 60 * 60 * 1000,
                    photos = emptyList(),
                )
            }
    }
}
