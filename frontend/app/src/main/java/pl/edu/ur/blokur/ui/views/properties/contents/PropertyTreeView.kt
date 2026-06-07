package pl.edu.ur.blokur.ui.views.properties.contents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.dtos.BuildingTreeNodeDto
import pl.edu.ur.blokur.dtos.PropertyResponseDto
import pl.edu.ur.blokur.dtos.StaircaseNodeDto
import pl.edu.ur.blokur.dtos.ApartmentNodeDto
import pl.edu.ur.blokur.ui.views.properties.utils.AddTarget
import pl.edu.ur.blokur.ui.views.properties.utils.DeleteTarget

@Composable
fun PropertyTreeView(
    properties: List<PropertyResponseDto>,
    buildings: List<BuildingTreeNodeDto>,
    expandedProperties: Set<String>,
    expandedBuildings: Set<String>,
    expandedStaircases: Set<String>,
    onToggleProperty: (String) -> Unit,
    onToggleBuilding: (String) -> Unit,
    onToggleStaircase: (String) -> Unit,
    onSelectProperty: (PropertyResponseDto) -> Unit,
    onSelectBuilding: (BuildingTreeNodeDto) -> Unit,
    onSelectStaircase: (StaircaseNodeDto, String) -> Unit,
    onSelectApartment: (ApartmentNodeDto, String) -> Unit,
    onAdd: (AddTarget, String?) -> Unit,
    onDelete: (DeleteTarget) -> Unit,
    modifier: Modifier = Modifier
) {
    // Group buildings by propertyId
    val buildingsByProperty = buildings.groupBy { it.propertyId }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Add Property button
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Struktura nieruchomości",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                FilledTonalIconButton(onClick = { onAdd(AddTarget.PROPERTY, null) }) {
                    Icon(Icons.Outlined.Add, contentDescription = "Dodaj wspólnotę")
                }
            }
        }

        // Render properties
        properties.forEach { property ->
            val propertyBuildings = buildingsByProperty[property.id] ?: emptyList()
            val isExpanded = property.name in expandedProperties

            item(key = "prop_${property.id}") {
                TreeNodeRow(
                    icon = Icons.Outlined.Business,
                    label = property.name,
                    subtitle = property.address,
                    isExpanded = isExpanded,
                    hasChildren = propertyBuildings.isNotEmpty(),
                    depth = 0,
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = { onSelectProperty(property) },
                    onToggle = { onToggleProperty(property.name) },
                    onAdd = { onAdd(AddTarget.BUILDING, property.id) },
                    onDelete = null
                )
            }

            if (isExpanded) {
                propertyBuildings.forEach { building ->
                    val isBuildingExpanded = building.id in expandedBuildings

                    item(key = "bld_${building.id}") {
                        TreeNodeRow(
                            icon = Icons.Outlined.Apartment,
                            label = building.name,
                            subtitle = building.address,
                            isExpanded = isBuildingExpanded,
                            hasChildren = building.staircases.isNotEmpty(),
                            depth = 1,
                            tint = MaterialTheme.colorScheme.secondary,
                            onClick = { onSelectBuilding(building) },
                            onToggle = { onToggleBuilding(building.id) },
                            onAdd = { onAdd(AddTarget.STAIRCASE, building.id) },
                            onDelete = { onDelete(DeleteTarget.Building(building.id, building.name)) }
                        )
                    }

                    if (isBuildingExpanded) {
                        building.staircases.forEach { staircase ->
                            val isStaircaseExpanded = staircase.id in expandedStaircases

                            item(key = "stc_${staircase.id}") {
                                TreeNodeRow(
                                    icon = Icons.Outlined.Home,
                                    label = "Klatka ${staircase.label}",
                                    subtitle = "${staircase.apartments.size} lokali",
                                    isExpanded = isStaircaseExpanded,
                                    hasChildren = staircase.apartments.isNotEmpty(),
                                    depth = 2,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    onClick = { onSelectStaircase(staircase, building.id) },
                                    onToggle = { onToggleStaircase(staircase.id) },
                                    onAdd = { onAdd(AddTarget.APARTMENT, staircase.id) },
                                    onDelete = { onDelete(DeleteTarget.Staircase(building.id, staircase.id, staircase.label)) }
                                )
                            }

                            if (isStaircaseExpanded) {
                                items(
                                    items = staircase.apartments,
                                    key = { "apt_${it.id}" }
                                ) { apartment ->
                                    TreeNodeRow(
                                        icon = Icons.Outlined.MeetingRoom,
                                        label = "Lokal ${apartment.number}",
                                        subtitle = buildString {
                                            apartment.floor?.let { append("Piętro $it") }
                                            apartment.areaM2?.let {
                                                if (isNotEmpty()) append(" · ")
                                                append("$it m²")
                                            }
                                        }.ifEmpty { null },
                                        isExpanded = false,
                                        hasChildren = false,
                                        depth = 3,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        onClick = { onSelectApartment(apartment, staircase.id) },
                                        onToggle = {},
                                        onAdd = null,
                                        onDelete = { onDelete(DeleteTarget.Apartment(staircase.id, apartment.id, apartment.number)) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Unassigned buildings (no matching property)
        val assignedIds = properties.map { it.id }.toSet()
        val unassigned = buildings.filter { it.propertyId == null || it.propertyId !in assignedIds }
        if (unassigned.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Budynki bez przypisanej wspólnoty",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                )
            }
            unassigned.forEach { building ->
                val isBuildingExpanded = building.id in expandedBuildings
                item(key = "ubld_${building.id}") {
                    TreeNodeRow(
                        icon = Icons.Outlined.Apartment,
                        label = building.name,
                        subtitle = building.address,
                        isExpanded = isBuildingExpanded,
                        hasChildren = building.staircases.isNotEmpty(),
                        depth = 0,
                        tint = MaterialTheme.colorScheme.secondary,
                        onClick = { onSelectBuilding(building) },
                        onToggle = { onToggleBuilding(building.id) },
                        onAdd = { onAdd(AddTarget.STAIRCASE, building.id) },
                        onDelete = { onDelete(DeleteTarget.Building(building.id, building.name)) }
                    )
                }
                if (isBuildingExpanded) {
                    building.staircases.forEach { staircase ->
                        val isStaircaseExpanded = staircase.id in expandedStaircases
                        item(key = "ustc_${staircase.id}") {
                            TreeNodeRow(
                                icon = Icons.Outlined.Home,
                                label = "Klatka ${staircase.label}",
                                subtitle = "${staircase.apartments.size} lokali",
                                isExpanded = isStaircaseExpanded,
                                hasChildren = staircase.apartments.isNotEmpty(),
                                depth = 1,
                                tint = MaterialTheme.colorScheme.tertiary,
                                onClick = { onSelectStaircase(staircase, building.id) },
                                onToggle = { onToggleStaircase(staircase.id) },
                                onAdd = { onAdd(AddTarget.APARTMENT, staircase.id) },
                                onDelete = { onDelete(DeleteTarget.Staircase(building.id, staircase.id, staircase.label)) }
                            )
                        }
                        if (isStaircaseExpanded) {
                            items(staircase.apartments, key = { "uapt_${it.id}" }) { apartment ->
                                TreeNodeRow(
                                    icon = Icons.Outlined.MeetingRoom,
                                    label = "Lokal ${apartment.number}",
                                    subtitle = apartment.floor?.let { "Piętro $it" },
                                    isExpanded = false, hasChildren = false, depth = 2,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    onClick = { onSelectApartment(apartment, staircase.id) },
                                    onToggle = {}, onAdd = null,
                                    onDelete = { onDelete(DeleteTarget.Apartment(staircase.id, apartment.id, apartment.number)) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TreeNodeRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    subtitle: String?,
    isExpanded: Boolean,
    hasChildren: Boolean,
    depth: Int,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    onToggle: () -> Unit,
    onAdd: (() -> Unit)?,
    onDelete: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 20).dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.small
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (hasChildren) {
            IconButton(
                onClick = onToggle,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = if (isExpanded) "Zwiń" else "Rozwiń",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Spacer(modifier = Modifier.size(24.dp))
        }

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (onAdd != null) {
            IconButton(
                onClick = onAdd,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = "Dodaj",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        if (onDelete != null) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "Usuń",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
