package pl.edu.ur.blokur.dto;

import java.util.UUID;

/**
 * Obiekt transferu danych (DTO) reprezentujący żądanie oddania głosu
 * przez zalogowanego użytkownika. Identyfikator głosującego pobierany jest
 * z kontekstu bezpieczeństwa (Principal), nie z ciała żądania.
 */
public class CastVoteRequest {

    private UUID optionId;

    /**
     * Tworzy instancję żądania bez argumentów (wymagane przez deserializator JSON).
     */
    public CastVoteRequest() {
    }

    /**
     * Tworzy instancję żądania z podanym identyfikatorem opcji.
     *
     * @param optionId identyfikator UUID wybranej opcji głosowania
     */
    public CastVoteRequest(UUID optionId) {
        this.optionId = optionId;
    }

    /**
     * Zwraca identyfikator wybranej opcji głosowania.
     *
     * @return identyfikator UUID opcji
     */
    public UUID getOptionId() {
        return optionId;
    }

    /**
     * Ustawia identyfikator wybranej opcji głosowania.
     *
     * @param optionId identyfikator UUID opcji
     */
    public void setOptionId(UUID optionId) {
        this.optionId = optionId;
    }
}
