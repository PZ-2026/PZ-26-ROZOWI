package pl.edu.ur.blokur.presentation

import kotlinx.serialization.Serializable

sealed interface PresentationRoutes {
    @Serializable
    data object Sample : PresentationRoutes
}