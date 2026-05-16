package pl.edu.ur.blokur.dto;

import java.util.UUID;
import lombok.Data;
import lombok.NonNull;

/**
 * Obiekt transferu danych (DTO) reprezentujący żądanie oddania głosu przez zalogowanego
 * użytkownika. Identyfikator głosującego pobierany jest z kontekstu bezpieczeństwa (Principal), nie
 * z ciała żądania.
 */
@Data
public class CastVoteRequest {
    @NonNull
    private UUID optionId;
}
