package pl.edu.ur.blokur.service;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.ur.blokur.dto.CategoryRequest;
import pl.edu.ur.blokur.dto.CategoryResponse;
import pl.edu.ur.blokur.dto.SlaRequest;
import pl.edu.ur.blokur.exception.NotFoundException;
import pl.edu.ur.blokur.models.TicketCategory;
import pl.edu.ur.blokur.repository.TicketCategoryRepository;

/** Serwis zarządzający kategoriami zgłoszeń serwisowych (CRUD + soft delete). */
@Service
public class TicketCategoryService {

    private final TicketCategoryRepository categoryRepository;

    /**
     * Tworzy serwis z wymaganymi zależnościami.
     *
     * @param categoryRepository repozytorium kategorii zgłoszeń
     */
    public TicketCategoryService(TicketCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Zwraca wszystkie aktywne kategorie (niezdeaktywowane).
     *
     * @return lista aktywnych kategorii
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findByIsActiveTrue().stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName(), c.getSlaHours()))
                .toList();
    }

    /**
     * Tworzy nową kategorię zgłoszeń.
     *
     * @param request dane nowej kategorii
     * @return reprezentacja utworzonej kategorii
     */
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        var category = new TicketCategory();
        category.setName(request.getName());
        var saved = categoryRepository.save(category);
        return new CategoryResponse(saved.getId(), saved.getName());
    }

    /**
     * Aktualizuje istniejącą kategorię.
     *
     * @param id identyfikator kategorii
     * @param request nowe dane kategorii
     * @return zaktualizowana kategoria
     * @throws NotFoundException gdy kategoria o podanym id nie istnieje
     */
    @Transactional
    public CategoryResponse updateCategory(UUID id, CategoryRequest request) {
        var category =
                categoryRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "Kategoria o id " + id + " nie istnieje"));
        category.setName(request.getName());
        return new CategoryResponse(category.getId(), category.getName());
    }

    /**
     * Ustawia docelowy czas reakcji SLA (w godzinach roboczych) dla wskazanej kategorii.
     *
     * @param id identyfikator kategorii
     * @param request żądanie zawierające liczbę godzin roboczych SLA
     * @throws NotFoundException gdy kategoria o podanym id nie istnieje
     */
    @Transactional
    public void setSlaHours(UUID id, SlaRequest request) {
        var category =
                categoryRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "Kategoria o id " + id + " nie istnieje"));
        category.setSlaHours(request.getSlaHours());
    }

    /**
     * Deaktywuje kategorię (soft delete — ustawia flagę {@code is_active} na {@code false}).
     *
     * @param id identyfikator kategorii
     * @throws NotFoundException gdy kategoria o podanym id nie istnieje
     */
    @Transactional
    public void deactivateCategory(UUID id) {
        var category =
                categoryRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "Kategoria o id " + id + " nie istnieje"));
        category.setActive(false);
    }
}
