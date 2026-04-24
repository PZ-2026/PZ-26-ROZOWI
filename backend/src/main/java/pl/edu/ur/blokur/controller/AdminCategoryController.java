package pl.edu.ur.blokur.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.ur.blokur.dto.CategoryRequest;
import pl.edu.ur.blokur.dto.CategoryResponse;
import pl.edu.ur.blokur.service.TicketCategoryService;

/**
 * Kontroler CRUD kategorii zgłoszeń dostępny wyłącznie dla roli {@code ZARZADCA}. Umożliwia
 * tworzenie, aktualizację i deaktywację kategorii.
 */
@RestController
@RequestMapping("/api/admin/categories")
@PreAuthorize("hasRole('ZARZADCA')")
public class AdminCategoryController {

    private final TicketCategoryService categoryService;

    /**
     * Tworzy kontroler z wymaganymi zależnościami.
     *
     * @param categoryService serwis zarządzający kategoriami zgłoszeń
     */
    public AdminCategoryController(TicketCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Tworzy nową kategorię zgłoszeń.
     *
     * @param request dane nowej kategorii
     * @return utworzona kategoria wraz ze statusem 201 Created
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Aktualizuje istniejącą kategorię (np. zmienia jej nazwę).
     *
     * @param id identyfikator kategorii
     * @param request nowe dane kategorii
     * @return zaktualizowana kategoria
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable UUID id, @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    /**
     * Deaktywuje kategorię (soft delete — ustawia flagę {@code is_active} na {@code false}).
     *
     * @param id identyfikator kategorii
     * @return odpowiedź 204 No Content
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateCategory(@PathVariable UUID id) {
        categoryService.deactivateCategory(id);
        return ResponseEntity.noContent().build();
    }
}
