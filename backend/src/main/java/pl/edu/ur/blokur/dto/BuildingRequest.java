package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;

/** DTO z danymi budynku przesyłanymi przez klienta (tworzenie i aktualizacja). */
@Data
public class BuildingRequest {

    @NotBlank(message = "Nazwa osiedla nie może być pusta")
    @Size(max = 255, message = "Nazwa osiedla nie może przekraczać 255 znaków")
    private String estateName;

    @NotBlank(message = "Nazwa budynku nie może być pusta")
    @Size(max = 255, message = "Nazwa budynku nie może przekraczać 255 znaków")
    private String name;

    @NotBlank(message = "Adres budynku nie może być pusty")
    @Size(max = 255, message = "Adres nie może przekraczać 255 znaków")
    private String address;

    private BigDecimal latitude;
    private BigDecimal longitude;
    private UUID propertyId;
}
