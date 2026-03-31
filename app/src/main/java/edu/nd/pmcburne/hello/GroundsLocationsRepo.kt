package edu.nd.pmcburne.hello

import kotlinx.coroutines.flow.Flow

class GroundsLocationsRepo(
    private val groundsDao: GroundsLocationService,
    private val uvaLocationApi: UvaApiService
) {
    val uvaGroundsLocationsAll: Flow<List<GroundsLocationEntity>> = groundsDao.fetchLocationsOnGrounds()

    suspend fun groundLocationsSync() {
        val fetchedLocations = uvaLocationApi.getLocations().map { placemarkDto ->
            placemarkDto.toEntity()
        }

        groundsDao.upsertGroundLocations(fetchedLocations)
    }

}