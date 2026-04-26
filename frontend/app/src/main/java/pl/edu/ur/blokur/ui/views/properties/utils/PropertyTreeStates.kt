package pl.edu.ur.blokur.ui.views.properties.utils

import pl.edu.ur.blokur.dtos.ApartmentNodeDto
import pl.edu.ur.blokur.dtos.BuildingTreeNodeDto
import pl.edu.ur.blokur.dtos.PropertyResponseDto
import pl.edu.ur.blokur.dtos.StaircaseNodeDto
import java.math.BigDecimal

/** Stan ekranu drzewa nieruchomości. */
sealed interface PropertyTreeState {
    data object Loading : PropertyTreeState
    data class Success(
        val properties: List<PropertyResponseDto>,
        val buildings: List<BuildingTreeNodeDto>
    ) : PropertyTreeState
    data class Error(val message: String) : PropertyTreeState
}

/** Jednorazowe zdarzenia. */
sealed interface PropertyTreeEvent {
    data class ShowSnackbar(val message: String) : PropertyTreeEvent
    data object TreeRefreshed : PropertyTreeEvent
}

/** Aktualnie zaznaczony węzeł drzewa. */
sealed interface SelectedNode {
    data object None : SelectedNode
    data class Property(val property: PropertyResponseDto) : SelectedNode
    data class Building(val building: BuildingTreeNodeDto) : SelectedNode
    data class Staircase(val staircase: StaircaseNodeDto, val buildingId: String) : SelectedNode
    data class Apartment(val apartment: ApartmentNodeDto, val staircaseId: String) : SelectedNode
}

/** Tryb formularza w panelu kontekstowym. */
enum class FormMode {
    VIEW, ADD, EDIT
}

/** Typ elementu do dodania. */
enum class AddTarget {
    PROPERTY, BUILDING, STAIRCASE, APARTMENT
}

// ─── Formularze ──────────────────────────────────────────────────────

data class PropertyFormFields(
    val name: String = "",
    val address: String = "",
    val nip: String = "",
    val managerPhone: String = "",
    val managerEmail: String = ""
)

data class BuildingFormFields(
    val estateName: String = "",
    val name: String = "",
    val address: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val propertyId: String? = null
)

data class StaircaseFormFields(
    val label: String = ""
)

data class ApartmentFormFields(
    val number: String = "",
    val floor: String = "",
    val areaM2: String = "",
    val ownershipType: String = "WLASNOSCIOWY"
)
