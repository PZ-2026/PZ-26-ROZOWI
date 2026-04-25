package pl.edu.ur.blokur.domain.model

/**
 * Typ własności lokalu zgodny z kontraktem backendu.
 */
enum class ApartmentOwnershipType {
    WLASNOSCIOWY,
    NAJEM
}

/**
 * Wspólnota mieszkaniowa prezentowana jako korzeń w drzewie nieruchomości.
 *
 * @property id identyfikator wspólnoty; może być pusty, gdy backendowe drzewo nie da się
 * jednoznacznie sparować z rekordem z `/api/properties`.
 * @property name nazwa wspólnoty.
 * @property address adres wspólnoty.
 * @property nip NIP wspólnoty.
 * @property managerPhone telefon kontaktowy zarządcy.
 * @property managerEmail adres e-mail zarządcy.
 * @property logoPath ścieżka logo przechowywanego po stronie backendu.
 * @property buildings budynki przypisane do wspólnoty.
 */
data class ManagedProperty(
    val id: String,
    val name: String,
    val address: String,
    val nip: String,
    val managerPhone: String?,
    val managerEmail: String?,
    val logoPath: String?,
    val buildings: List<ManagedBuilding> = emptyList()
)

/**
 * Budynek w drzewie zarządcy.
 *
 * @property id identyfikator budynku.
 * @property propertyId identyfikator wspólnoty nadrzędnej.
 * @property estateName nazwa wspólnoty/osiedla widoczna w backendowym DTO drzewa.
 * @property name nazwa budynku.
 * @property address adres budynku.
 * @property latitude szerokość geograficzna.
 * @property longitude długość geograficzna.
 * @property staircases klatki schodowe należące do budynku.
 */
data class ManagedBuilding(
    val id: String,
    val propertyId: String?,
    val estateName: String,
    val name: String,
    val address: String,
    val latitude: Double?,
    val longitude: Double?,
    val staircases: List<ManagedStaircase> = emptyList()
)

/**
 * Klatka schodowa w drzewie zarządcy.
 *
 * @property id identyfikator klatki.
 * @property buildingId identyfikator budynku nadrzędnego.
 * @property label etykieta klatki, np. `A` albo `B`.
 * @property apartments lokale przypisane do klatki.
 */
data class ManagedStaircase(
    val id: String,
    val buildingId: String?,
    val label: String,
    val apartments: List<ManagedApartment> = emptyList()
)

/**
 * Lokal mieszkalny widoczny na najniższym poziomie drzewa.
 *
 * @property id identyfikator lokalu.
 * @property staircaseId identyfikator klatki nadrzędnej.
 * @property number numer lokalu.
 * @property floor piętro.
 * @property areaM2 powierzchnia lokalu w metrach kwadratowych.
 * @property ownershipType typ własności.
 * @property currentBalance aktualne saldo rozliczeniowe lokalu.
 */
data class ManagedApartment(
    val id: String,
    val staircaseId: String?,
    val number: String,
    val floor: Int?,
    val areaM2: Double?,
    val ownershipType: ApartmentOwnershipType?,
    val currentBalance: Double?
)

/**
 * Dane formularza tworzenia lub edycji wspólnoty.
 */
data class PropertyDraft(
    val name: String,
    val address: String,
    val nip: String,
    val managerPhone: String?,
    val managerEmail: String?
)

/**
 * Dane formularza tworzenia lub edycji budynku.
 */
data class BuildingDraft(
    val propertyId: String,
    val estateName: String,
    val name: String,
    val address: String,
    val latitude: Double?,
    val longitude: Double?
)

/**
 * Dane formularza tworzenia lub edycji klatki schodowej.
 */
data class StaircaseDraft(
    val label: String
)

/**
 * Dane formularza tworzenia lub edycji lokalu.
 */
data class ApartmentDraft(
    val number: String,
    val floor: Int?,
    val areaM2: Double?,
    val ownershipType: ApartmentOwnershipType?
)
