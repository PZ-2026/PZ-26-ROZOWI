package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Data;

/**
 * DTO żądania tworzenia nowego zgłoszenia przez mieszkańca (POST /api/tickets).
 *
 * <p>Lokal jest pobierany automatycznie z konta zalogowanego użytkownika — nie jest wymagany w
 * żądaniu.
 */
@Data
public class TicketRequest {

    @NotBlank(message = "Tytuł zgłoszenia jest wymagany")
    @Size(max = 100, message = "Tytuł zgłoszenia może mieć maks. 100 znaków")
    private String title;

    @NotBlank(message = "Opis zgłoszenia jest wymagany")
    @Size(max = 2000, message = "Opis zgłoszenia może mieć maks. 2000 znaków")
    private String description;

    @NotNull(message = "Kategoria zgłoszenia jest wymagana")
    private UUID categoryId;

    /**
     * Konstruktor parametryczny.
     *
     * @param title tytuł zgłoszenia
     * @param description opis zgłoszenia
     * @param categoryId identyfikator kategorii
     */
    public TicketRequest(String title, String description, UUID categoryId) {
        this.title = title;
        this.description = description;
        this.categoryId = categoryId;
    }
}
