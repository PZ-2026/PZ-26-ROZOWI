package pl.edu.ur.blokur.dto

data class WorkAcceptanceProtocolRequest(
    val ticketNumber: String,
    val workDescription: String,
    val maintenanceWorkerName: String,
)
