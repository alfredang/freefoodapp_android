package com.tertiaryinfotech.freefood.data

import android.content.Context
import com.tertiaryinfotech.freefood.model.Coordinate
import com.tertiaryinfotech.freefood.model.FoodListing
import com.tertiaryinfotech.freefood.model.Recurrence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Single source of truth for food listings. The iOS app stores these in the CloudKit
 * public database so every device sees the same posts; this Android port keeps them in a
 * local Room database behind the same repository surface so a shared cloud backend
 * (Firebase Firestore, a REST API, etc.) can be swapped in without touching the UI.
 */
class FoodListingRepository(private val dao: FoodListingDao) {

    /** All stored listings as domain models. Sorting/filtering happens in the ViewModel. */
    val listings: Flow<List<FoodListing>> =
        dao.observeAll().map { rows -> rows.map { it.toModel() } }

    suspend fun add(listing: FoodListing) = dao.upsert(FoodListingEntity.from(listing))

    suspend fun delete(listing: FoodListing) = dao.deleteById(listing.id)

    suspend fun like(listing: FoodListing) = dao.incrementLikes(listing.id)

    /** Remove one-time listings whose end time has passed or that are older than 7 days. */
    suspend fun purgeExpired(now: Long = System.currentTimeMillis()) {
        dao.getAll().forEach { row ->
            if (!row.toModel().isActive(now)) dao.deleteById(row.id)
        }
    }

    /** Seed a few sample listings on first launch so the feed and map aren't empty. */
    suspend fun seedIfEmpty() {
        if (dao.getAll().isNotEmpty()) return
        sampleListings().forEach { dao.upsert(FoodListingEntity.from(it)) }
    }

    private fun sampleListings(): List<FoodListing> {
        val zone = java.time.ZoneId.systemDefault()
        // Local time today/tomorrow at the given hour:minute, as epoch millis.
        fun day(offset: Long, h: Int, m: Int): Long =
            java.time.LocalDate.now().plusDays(offset).atTime(h, m).atZone(zone).toInstant().toEpochMilli()
        // Country is left blank so the sample posts are visible regardless of the
        // device's region (the "only my country" filter only hides posts whose country
        // is set and differs from the device's).
        return listOf(
            FoodListing(
                title = "Pastries after tech meetup",
                details = "Mixed croissants, muffins and sandwiches left from a morning event. Bring your own bag.",
                locationName = "Tanjong Pagar Centre",
                coordinate = Coordinate(1.2764, 103.8458),
                date = day(1, 0, 0), startTime = day(0, 18, 0), endTime = day(0, 20, 0),
                recurrence = Recurrence.NONE, likes = 4,
            ),
            FoodListing(
                title = "Free bread, daily closing",
                details = "Unsold artisan loaves given away every evening before we close. First come first served.",
                locationName = "Tiong Bahru Bakery",
                coordinate = Coordinate(1.2858, 103.8310),
                date = day(0, 0, 0), startTime = day(0, 20, 0), endTime = day(0, 21, 0),
                recurrence = Recurrence.DAILY, likes = 12,
            ),
            FoodListing(
                title = "Catering leftovers - office event",
                details = "Trays of rice, noodles and salad from a corporate lunch. Plenty for a dozen people.",
                locationName = "Marina Bay Financial Centre",
                coordinate = Coordinate(1.2799, 103.8547),
                date = day(1, 0, 0), startTime = day(0, 12, 0), endTime = day(0, 14, 0),
                recurrence = Recurrence.NONE, likes = 7,
            ),
            FoodListing(
                title = "Weekly community fridge restock",
                details = "Fresh fruit and vegetables shared by the neighbourhood every Saturday. Take what you need.",
                locationName = "Tampines Hub",
                coordinate = Coordinate(1.3536, 103.9410),
                date = day(0, 0, 0), startTime = day(0, 10, 0), endTime = day(0, 12, 0),
                recurrence = Recurrence.WEEKLY, likes = 21,
            ),
        )
    }

    companion object {
        fun from(context: Context): FoodListingRepository =
            FoodListingRepository(AppDatabase.get(context).listingDao())
    }
}
