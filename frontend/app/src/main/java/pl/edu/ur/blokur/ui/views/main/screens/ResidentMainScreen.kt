package pl.edu.ur.blokur.ui.views.main.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import pl.edu.ur.blokur.ui.components.EmptyState
import pl.edu.ur.blokur.ui.components.TopBar
import pl.edu.ur.blokur.ui.views.main.contents.BottomNavBar
import pl.edu.ur.blokur.ui.views.main.utils.NavBarOption
import pl.edu.ur.blokur.ui.views.main.utils.ResidentMainEvent
import pl.edu.ur.blokur.ui.views.main.utils.ResidentMainState
import pl.edu.ur.blokur.ui.views.main.viewmodels.ResidentMainViewModel

/**
 * Główny ekran aplikacji po zalogowaniu.
 *
 * Zawiera [TopBar] z przyciskiem wylogowania, dolną nawigację z zakładkami
 * filtrowanymi per rola oraz slot [innerContent] wypełniany przez zagnieżdżony NavHost.
 *
 * @param viewModel        ViewModel wstrzykiwany przez Hilt.
 * @param onNavBarItemClicked callback zmiany zakładki (obsługiwany w [pl.edu.ur.blokur.presentation.resident.Resident]).
 * @param onLogout         callback wywoływany po wylogowaniu – nawiguje do ekranu logowania.
 * @param innerContent     zawartość aktywnej zakładki renderowana wewnątrz [Scaffold].
 */
@Composable
fun ResidentMainScreen(
    viewModel: ResidentMainViewModel,
    onNavBarItemClicked: (NavBarOption) -> Unit,
    onLogout: () -> Unit,
    innerContent: @Composable (Modifier) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val availableNavItems by viewModel.availableNavItems.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ResidentMainEvent.ChangeResidentView -> onNavBarItemClicked(event.option)
                is ResidentMainEvent.Logout -> onLogout()
            }
        }
    }

    val title = when (state) {
        is ResidentMainState.ViewingAnnouncements -> "Ogłoszenia"
        is ResidentMainState.ViewingFinances -> "Finanse"
        is ResidentMainState.ViewingProfile -> "Profil"
        is ResidentMainState.ViewingTickets -> "Zgłoszenia"
        is ResidentMainState.ViewingProperties -> "Lokale"
        is ResidentMainState.ViewingUsers -> "Użytkownicy"
        is ResidentMainState.ViewingCategories -> "Kategorie"
        is ResidentMainState.ViewingResolutions -> "Uchwały"
        is ResidentMainState.ViewingInspections -> "Przeglądy"
        is ResidentMainState.ViewingNotifications -> "Powiadomienia"
        is ResidentMainState.Error -> "Błąd"
        else -> "BlokUR"
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopBar(
                title = title,
                actions = {
                    IconButton(onClick = viewModel::logout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = "Wyloguj",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomNavBar(
                state = state,
                items = availableNavItems,
                onItemClicked = viewModel::onOptionClicked
            )
        }
    ) { innerPadding ->
        if (state is ResidentMainState.Error) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                EmptyState(
                    title = "Błąd krytyczny",
                    description = (state as ResidentMainState.Error).message
                )
            }
        } else {
            innerContent(Modifier.padding(innerPadding))
        }
    }
}
