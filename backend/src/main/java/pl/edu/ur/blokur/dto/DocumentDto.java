package pl.edu.ur.blokur.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO reprezentujące dokument.
 *
 * @param id identyfikator dokumentu
 * @param type typ dokumentu
 * @param title tytuł dokumentu
 * @param createdAt data utworzenia
 * @param downloadUrl link do pobrania dokumentu
 */
public record DocumentDto(
        UUID id, String type, String title, LocalDateTime createdAt, String downloadUrl) {}
