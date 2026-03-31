package edu.nd.pmcburne.hello

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface GroundsLocationService {

    @Query("SELECT * FROM locations ORDER BY locationName ASC")
    fun fetchLocationsOnGrounds(): Flow<List<GroundsLocationEntity>>

    @Upsert
    suspend fun upsertGroundLocations(locations: List<GroundsLocationEntity>)
}