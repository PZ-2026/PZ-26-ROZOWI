package pl.edu.ur.blokur.ui.utils

import java.math.BigDecimal
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object PolishFormat {
    private val moneyFormat: NumberFormat =
        NumberFormat.getNumberInstance(Locale("pl", "PL")).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }

    private val dateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale("pl", "PL"))

    private val dateTimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm", Locale("pl", "PL"))

    fun formatMoney(amount: BigDecimal): String = "${moneyFormat.format(amount)} zł"

    fun formatDate(value: String?): String {
        if (value.isNullOrBlank()) return "—"
        return try {
            when {
                value.contains("T") -> LocalDateTime.parse(value.take(19)).format(dateTimeFormatter)
                value.length >= 10 -> LocalDate.parse(value.take(10)).format(dateFormatter)
                else -> value
            }
        } catch (_: Exception) {
            try {
                SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(value.take(10))?.let {
                    SimpleDateFormat("dd.MM.yyyy", Locale("pl", "PL")).format(it)
                } ?: value.take(10)
            } catch (_: Exception) {
                value.take(10)
            }
        }
    }
}
