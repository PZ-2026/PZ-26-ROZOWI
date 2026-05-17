package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import pl.edu.ur.blokur.models.AnnouncementTargetType;

/**
 * DTO służące do tworzenia i edycji ogłoszenia przez zarządcę. Zawiera walidację pól wymaganych.
 */
@Data
public class AnnouncementRequest {

    @NotBlank(message = "Tytuł ogłoszenia nie może być pusty")
    private String title;

    @NotBlank(message = "Treść ogłoszenia nie może być pusta")
    private String content;

    @NotNull(message = "Typ zasięgu musi zostać określony")
    private AnnouncementTargetType targetType;

    private UUID targetId;

    private LocalDateTime plannedDate;
}
