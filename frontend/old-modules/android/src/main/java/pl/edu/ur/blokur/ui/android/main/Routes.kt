package pl.edu.ur.blokur.ui.android.main

import kotlinx.serialization.Serializable

sealed interface ApplicationRoutes {
    @Serializable
    data object Resident : ApplicationRoutes
}