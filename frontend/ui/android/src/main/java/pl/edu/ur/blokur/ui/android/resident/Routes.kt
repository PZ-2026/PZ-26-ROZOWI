package pl.edu.ur.blokur.ui.android.resident

import kotlinx.serialization.Serializable

sealed interface ResidentMainRoutes {
    @Serializable
    data object ResidentMainView : ResidentMainRoutes
}

sealed interface AnnouncementsRoutes {
    @Serializable
    data object AnnouncementsView : AnnouncementsRoutes
}

sealed interface FinancesRoutes {
    @Serializable
    data object FinancesView : FinancesRoutes
}

sealed interface ProfileRoutes {
    @Serializable
    data object ProfileView : ProfileRoutes
}

sealed interface TicketsRoutes {
    @Serializable
    data object TicketsView : TicketsRoutes
}