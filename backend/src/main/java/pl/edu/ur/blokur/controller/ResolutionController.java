package pl.edu.ur.blokur.controller;

import java.security.Principal;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pl.edu.ur.blokur.dto.CastVoteRequest;
import pl.edu.ur.blokur.service.ResolutionService;

/**
 * Kontroler obsługujący żądania HTTP dla modułu uchwał i głosowań.
 * Udostępnia endpoint chroniony tokenem JWT umożliwiający zalogowanemu
 * lokatorowi oddanie głosu w wybranej uchwale.
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
     * Rejestruje głos zalogowanego użytkownika w wybranej uchwale.
     * Identyfikator głosującego pobierany jest z kontekstu bezpieczeństwa
     * (Principal z tokena JWT), a nie z ciała żądania.
     * Próba oddania głosu po raz drugi skutkuje odpowiedzią HTTP 409 Conflict.
     *
     * @param id        identyfikator UUID uchwały, w której oddawany jest głos
     * @param request   ciało żądania zawierające {@code optionId} wybranej opcji
     * @param principal kontekst bezpieczeństwa zalogowanego użytkownika
     * @return odpowiedź HTTP 204 No Content po pomyślnym zapisie głosu,
     *         lub 403 Forbidden gdy użytkownik nie jest zalogowany
     */
    @PostMapping("/{id}/vote")
    public ResponseEntity<Void> castVote(@PathVariable UUID id,
                                         @RequestBody CastVoteRequest request,
                                         Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(403).build();
        }

        resolutionService.castVote(id, request, principal.getName());

        return ResponseEntity.noContent().build();
    }
}
