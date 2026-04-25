package pl.edu.ur.blokur.domain.usecase

import pl.edu.ur.blokur.domain.model.ApartmentDraft
import pl.edu.ur.blokur.domain.model.BuildingDraft
import pl.edu.ur.blokur.domain.model.ManagedApartment
import pl.edu.ur.blokur.domain.model.ManagedBuilding
import pl.edu.ur.blokur.domain.model.ManagedProperty
import pl.edu.ur.blokur.domain.model.ManagedStaircase
import pl.edu.ur.blokur.domain.model.PropertyDraft
import pl.edu.ur.blokur.domain.model.StaircaseDraft
import pl.edu.ur.blokur.domain.repository.PropertyStructureRepository
import javax.inject.Inject

class GetPropertyTreeUseCase @Inject constructor(
    private val repository: PropertyStructureRepository
) {
    suspend operator fun invoke(): List<ManagedProperty> = repository.getPropertyTree()
}

class CreatePropertyUseCase @Inject constructor(
    private val repository: PropertyStructureRepository
) {
    suspend operator fun invoke(draft: PropertyDraft): ManagedProperty =
        repository.createProperty(draft)
}

class UpdatePropertyUseCase @Inject constructor(
    private val repository: PropertyStructureRepository
) {
    suspend operator fun invoke(propertyId: String, draft: PropertyDraft): ManagedProperty =
        repository.updateProperty(propertyId, draft)
}

class CreateBuildingUseCase @Inject constructor(
    private val repository: PropertyStructureRepository
) {
    suspend operator fun invoke(draft: BuildingDraft): ManagedBuilding =
        repository.createBuilding(draft)
}

class UpdateBuildingUseCase @Inject constructor(
    private val repository: PropertyStructureRepository
) {
    suspend operator fun invoke(buildingId: String, draft: BuildingDraft): ManagedBuilding =
        repository.updateBuilding(buildingId, draft)
}

class DeleteBuildingUseCase @Inject constructor(
    private val repository: PropertyStructureRepository
) {
    suspend operator fun invoke(buildingId: String) = repository.deleteBuilding(buildingId)
}

class CreateStaircaseUseCase @Inject constructor(
    private val repository: PropertyStructureRepository
) {
    suspend operator fun invoke(buildingId: String, draft: StaircaseDraft): ManagedStaircase =
        repository.createStaircase(buildingId, draft)
}

class UpdateStaircaseUseCase @Inject constructor(
    private val repository: PropertyStructureRepository
) {
    suspend operator fun invoke(
        buildingId: String,
        staircaseId: String,
        draft: StaircaseDraft
    ): ManagedStaircase = repository.updateStaircase(buildingId, staircaseId, draft)
}

class DeleteStaircaseUseCase @Inject constructor(
    private val repository: PropertyStructureRepository
) {
    suspend operator fun invoke(buildingId: String, staircaseId: String) =
        repository.deleteStaircase(buildingId, staircaseId)
}

class CreateApartmentUseCase @Inject constructor(
    private val repository: PropertyStructureRepository
) {
    suspend operator fun invoke(staircaseId: String, draft: ApartmentDraft): ManagedApartment =
        repository.createApartment(staircaseId, draft)
}

class UpdateApartmentUseCase @Inject constructor(
    private val repository: PropertyStructureRepository
) {
    suspend operator fun invoke(
        staircaseId: String,
        apartmentId: String,
        draft: ApartmentDraft
    ): ManagedApartment = repository.updateApartment(staircaseId, apartmentId, draft)
}

class DeleteApartmentUseCase @Inject constructor(
    private val repository: PropertyStructureRepository
) {
    suspend operator fun invoke(staircaseId: String, apartmentId: String) =
        repository.deleteApartment(staircaseId, apartmentId)
}
