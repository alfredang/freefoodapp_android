package com.tertiaryinfotech.freefood.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodListingDao {
    @Query("SELECT * FROM listings")
    fun observeAll(): Flow<List<FoodListingEntity>>

    @Query("SELECT * FROM listings")
    suspend fun getAll(): List<FoodListingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(listing: FoodListingEntity)

    @Query("DELETE FROM listings WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE listings SET likes = likes + 1 WHERE id = :id")
    suspend fun incrementLikes(id: String)

    @Query("SELECT * FROM listings WHERE id = :id")
    suspend fun getById(id: String): FoodListingEntity?
}
