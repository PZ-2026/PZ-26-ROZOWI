package pl.edu.ur.blokur.service

import com.itextpdf.io.font.PdfEncodings
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import pl.edu.ur.blokur.dto.WorkAcceptanceProtocolRequest
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class PdfGeneratorService {
    fun generateWorkAcceptanceProtocol(request: WorkAcceptanceProtocolRequest): ByteArray {
        val outputStream = ByteArrayOutputStream()

        val writer = PdfWriter(outputStream)
        val pdfDocument = PdfDocument(writer)
        val document = Document(pdfDocument)

        val font = loadUnicodeFont()
        document.setFont(font)

        document.add(
            Paragraph("BLOKUR")
                .setBold()
                .setFontSize(18f)
                .setTextAlignment(TextAlignment.CENTER),
        )

        document.add(
            Paragraph("Dane wspólnoty / zarządcy")
                .setFontSize(11f)
                .setTextAlignment(TextAlignment.CENTER),
        )

        document.add(
            Paragraph("Miejsce na logo wspólnoty")
                .setItalic()
                .setFontSize(10f)
                .setTextAlignment(TextAlignment.CENTER),
        )

        document.add(Paragraph("\n"))

        document.add(
            Paragraph("PROTOKÓŁ ODBIORU PRAC")
                .setBold()
                .setFontSize(16f)
                .setTextAlignment(TextAlignment.CENTER),
        )

        document.add(Paragraph("\n"))

        val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

        document.add(
            Paragraph("Data wygenerowania dokumentu: $currentDate")
                .setFontSize(11f),
        )

        document.add(Paragraph("\n"))

        val table =
            Table(UnitValue.createPercentArray(floatArrayOf(30f, 70f)))
                .useAllAvailableWidth()

        table.addCell(Cell().add(Paragraph("Numer zgłoszenia")))
        table.addCell(Cell().add(Paragraph(request.ticketNumber)))

        table.addCell(Cell().add(Paragraph("Imię konserwatora")))
        table.addCell(Cell().add(Paragraph(request.maintenanceWorkerName)))

        table.addCell(Cell().add(Paragraph("Opis wykonanych prac")))
        table.addCell(Cell().add(Paragraph(request.workDescription)))

        document.add(table)

        document.add(Paragraph("\n\n"))

        document.add(Paragraph("Podpis konserwatora: ______________________________"))
        document.add(Paragraph("\n"))
        document.add(Paragraph("Podpis zarządcy / osoby odbierającej: ______________________________"))

        document.close()

        return outputStream.toByteArray()
    }

    private fun loadUnicodeFont(): PdfFont {
        val resource = ClassPathResource("fonts/NotoSans-Regular.ttf")
        val fontBytes = resource.inputStream.use { it.readBytes() }

        return PdfFontFactory.createFont(
            fontBytes,
            PdfEncodings.IDENTITY_H,
            PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED,
        )
    }
}
