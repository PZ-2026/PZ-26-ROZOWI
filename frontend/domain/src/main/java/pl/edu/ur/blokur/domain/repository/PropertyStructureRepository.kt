package pl.edu.ur.blokur.domain.repository

import pl.edu.ur.blokur.domain.model.ApartmentDraft
import pl.edu.ur.blokur.domain.model.ManagedApartment
import pl.edu.ur.blokur.domain.model.ManagedBuilding
import pl.edu.ur.blokur.domain.model.ManagedProperty
import pl.edu.ur.blokur.domain.model.ManagedStaircase
import pl.edu.ur.blokur.domain.model.PropertyDraft
import pl.edu.ur.blokur.domain.model.BuildingDraft
import pl.edu.ur.blokur.domain.model.StaircaseDraft

interface PropertyStructureRepository {

    suspend fun getPropertyTree(): List<ManagedProperty>

    suspend fun createProperty(draft: PropertyDraft): ManagedProperty

    suspend fun updateProperty(propertyId: String, draft: PropertyDraft): ManagedProperty

    suspend fun createBuilding(draft: BuildingDraft): ManagedBuilding

    suspend fun updateBuilding(buildingId: String, draft: BuildingDraft): ManagedBuilding

    suspend fun deleteBuilding(buildingId: String)

    suspend fun createStaircase(buildingId: String, draft: StaircaseDraft): ManagedStaircase

    suspend fun updateStaircase(
        buildingId: String,
        staircaseId: String,
        draft: StaircaseDraft
    ): ManagedStaircase

    suspend fun deleteStaircase(buildingId: String, staircaseId: String)

    suspend fun createApartment(staircaseId: String, draft: ApartmentDraft): ManagedApartment

    suspend fun updateApartment(
        staircaseId: String,
        apartmentId: String,
        draft: ApartmentDraft
    ): ManagedApartment

    suspend fun deleteApartment(staircaseId: String, apartmentId: String)
}
