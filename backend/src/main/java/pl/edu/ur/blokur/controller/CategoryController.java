package pl.edu.ur.blokur.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.ur.blokur.dto.CategoryResponse;
import pl.edu.ur.blokur.service.TicketCategoryService;

/** Publiczny (dla zalogowanych) endpoint udostępniający listę aktywnych kategorii zgłoszeń. */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final TicketCategoryService categoryService;

    /**
     * Tworzy kontroler z wymaganymi zależnościami.
     *
     * @param categoryService serwis zarządzający kategoriami zgłoszeń
     */
    public CategoryController(TicketCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Zwraca listę kategorii aktualnie aktywnych w systemie.
     *
     * @return lista aktywnych kategorii
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getActiveCategories() {
        return ResponseEntity.ok(categoryService.getActiveCategories());
    }
}
