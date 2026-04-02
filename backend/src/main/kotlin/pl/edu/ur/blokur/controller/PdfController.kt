package pl.edu.ur.blokur.controller

import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pl.edu.ur.blokur.dto.WorkAcceptanceProtocolRequest
import pl.edu.ur.blokur.service.PdfGeneratorService

@RestController
@RequestMapping("/api/pdf")
class PdfController(
    private val pdfGeneratorService: PdfGeneratorService
) {

    @PostMapping("/work-acceptance-protocol")
    fun generateWorkAcceptanceProtocol(
        @RequestBody request: WorkAcceptanceProtocolRequest
    ): ResponseEntity<ByteArray> {
        val pdfBytes = pdfGeneratorService.generateWorkAcceptanceProtocol(request)

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_PDF
            contentDisposition = ContentDisposition.inline()
                .filename("protokol-odbioru-prac.pdf")
                .build()
        }

        return ResponseEntity.ok()
            .headers(headers)
            .body(pdfBytes)
    }
}