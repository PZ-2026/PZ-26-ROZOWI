package pl.edu.ur.blokur.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.ur.blokur.dto.CastVoteRequest;
import pl.edu.ur.blokur.dto.CreateResolutionRequest;
import pl.edu.ur.blokur.dto.ResolutionDetailDto;
import pl.edu.ur.blokur.dto.ResolutionDto;
import pl.edu.ur.blokur.service.ResolutionService;

/**
 * Kontroler obsługujący żądania HTTP dla modułu uchwał i głosowań. Udostępnia endpoint chroniony
 * tokenem JWT umożliwiający zarządzanie uchwałami i oddawanie głosów.
 */
@RestController
@RequestMapping("/api/resolutions")
public class ResolutionController {

    private final ResolutionService resolutionService;

    /**
     * Tworzy instancję kontrolera z wymaganym serwisem.
     *
     * @param resolutionService serwis logiki biznesowej uchwał i głosowań
     */
    public ResolutionController(ResolutionService resolutionService) {
        this.resolutionService = resolutionService;
    }

    /**
     * Tworzy nową uchwałę. Wymaga roli Zarządcy.
     *
     * @param request ciało żądania z danymi uchwały
     * @param principal zalogowany użytkownik
     * @return 201 Created po utworzeniu
     */
    @PostMapping
    public ResponseEntity<Void> createResolution(
            @Valid @RequestBody CreateResolutionRequest request, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        resolutionService.createResolution(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Zwraca listę uchwał dostępnych dla zalogowanego użytkownika (dla Mieszkańca tylko powiązane z
     * jego budynkiem, dla Zarządcy wszystkie).
     *
     * @param principal zalogowany użytkownik
     * @return lista uchwał
     */
    @GetMapping
    public ResponseEntity<List<ResolutionDto>> getResolutions(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(resolutionService.getResolutionsForUser(principal.getName()));
    }

    /**
     * Pobiera szczegóły uchwały, w tym opcje głosowania i ewentualnie wyniki.
     *
     * @param id identyfikator uchwały
     * @param principal zalogowany użytkownik
     * @return szczegóły uchwały
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResolutionDetailDto> getResolutionDetails(
            @PathVariable UUID id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(resolutionService.getResolutionDetails(id, principal.getName()));
    }

    /**
     * Generuje plik PDF z wynikiem głosowania dla zarządcy.
     *
     * @param id identyfikator uchwały
     * @param principal zalogowany użytkownik
     * @return plik PDF
     */
    @GetMapping(value = "/{id}/report", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getResolutionReport(@PathVariable UUID id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        byte[] pdfBytes = resolutionService.generateResolutionReport(id, principal.getName());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "raport_uchwala.pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    /**
     * Rejestruje głos zalogowanego użytkownika w wybranej uchwale. Identyfikator głosującego
     * pobierany jest z kontekstu bezpieczeństwa (Principal z tokena JWT), a nie z ciała żądania.
     * Próba oddania głosu po raz drugi skutkuje odpowiedzią HTTP 409 Conflict.
     *
     * @param id identyfikator UUID uchwały, w której oddawany jest głos
     * @param request ciało żądania zawierające {@code optionId} wybranej opcji
     * @param principal kontekst bezpieczeństwa zalogowanego użytkownika
     * @return odpowiedź HTTP 204 No Content po pomyślnym zapisie głosu, lub 403 Forbidden gdy
     *     użytkownik nie jest zalogowany
     */
    @PostMapping("/{id}/vote")
    public ResponseEntity<Void> castVote(
            @PathVariable UUID id, @RequestBody CastVoteRequest request, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        resolutionService.castVote(id, request, principal.getName());

        return ResponseEntity.noContent().build();
    }
}
