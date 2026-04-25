package pl.edu.ur.blokur.presentation.propertytree.util

import pl.edu.ur.blokur.domain.model.ApartmentOwnershipType
import pl.edu.ur.blokur.domain.model.ManagedProperty

/**
 * Zaznaczony węzeł w drzewie nieruchomości.
 */
sealed interface PropertyTreeSelection {
    data object Root : PropertyTreeSelection

    data class PropertyNode(val propertyId: String) : PropertyTreeSelection

    data class BuildingNode(
        val propertyId: String,
        val buildingId: String
    ) : PropertyTreeSelection

    data class StaircaseNode(
        val propertyId: String,
        val buildingId: String,
        val staircaseId: String
    ) : PropertyTreeSelection

    data class ApartmentNode(
        val propertyId: String,
        val buildingId: String,
        val staircaseId: String,
        val apartmentId: String
    ) : PropertyTreeSelection
}

/**
 * Stan formularza wspólnoty.
 */
data class PropertyFormState(
    val name: String = "",
    val address: String = "",
    val nip: String = "",
    val managerPhone: String = "",
    val managerEmail: String = ""
)

/**
 * Stan formularza budynku.
 */
data class BuildingFormState(
    val estateName: String = "",
    val name: String = "",
    val address: String = "",
    val latitude: String = "",
    val longitude: String = ""
)

/**
 * Stan formularza klatki schodowej.
 */
data class StaircaseFormState(
    val label: String = ""
)

/**
 * Stan formularza lokalu.
 */
data class ApartmentFormState(
    val number: String = "",
    val floor: String = "",
    val areaM2: String = "",
    val ownershipType: ApartmentOwnershipType? = ApartmentOwnershipType.WLASNOSCIOWY
)

/**
 * Stan ekranu drzewa nieruchomości.
 */
sealed interface PropertyTreeState {
    data object Loading : PropertyTreeState

    data class Error(val message: String) : PropertyTreeState

    data class Success(
        val properties: List<ManagedProperty>,
        val selectedNode: PropertyTreeSelection,
        val expandedPropertyIds: Set<String> = emptySet(),
        val expandedBuildingIds: Set<String> = emptySet(),
        val expandedStaircaseIds: Set<String> = emptySet(),
        val propertyForm: PropertyFormState = PropertyFormState(),
        val newPropertyForm: PropertyFormState = PropertyFormState(),
        val buildingForm: BuildingFormState = BuildingFormState(),
        val newBuildingForm: BuildingFormState = BuildingFormState(),
        val staircaseForm: StaircaseFormState = StaircaseFormState(),
        val newStaircaseForm: StaircaseFormState = StaircaseFormState(),
        val apartmentForm: ApartmentFormState = ApartmentFormState(),
        val newApartmentForm: ApartmentFormState = ApartmentFormState(),
        val isSaving: Boolean = false
    ) : PropertyTreeState
}

/**
 * Jednorazowe zdarzenia interfejsu dla ekranu drzewa nieruchomości.
 */
sealed interface PropertyTreeEvent {
    data class ShowMessage(val message: String) : PropertyTreeEvent
}
