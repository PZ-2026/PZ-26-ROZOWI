package pl.edu.ur.blokur.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.ur.blokur.dto.CategoryRequest;
import pl.edu.ur.blokur.dto.CategoryResponse;
import pl.edu.ur.blokur.exception.NotFoundException;
import pl.edu.ur.blokur.models.TicketCategory;
import pl.edu.ur.blokur.repository.TicketCategoryRepository;

import java.util.List;
import java.util.UUID;

@Service
public class TicketCategoryService {

    private final TicketCategoryRepository categoryRepository;

    public TicketCategoryService(TicketCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findByIsActiveTrue().stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName()))
                .toList();
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        TicketCategory category = new TicketCategory();
        category.setName(request.getName());
        TicketCategory saved = categoryRepository.save(category);
        return new CategoryResponse(saved.getId(), saved.getName());
    }

    @Transactional
    public CategoryResponse updateCategory(UUID id, CategoryRequest request) {
        TicketCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Kategoria o id " + id + " nie istnieje"));
        category.setName(request.getName());
        return new CategoryResponse(category.getId(), category.getName());
    }

    @Transactional
    public void deactivateCategory(UUID id) {
        TicketCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Kategoria o id " + id + " nie istnieje"));
        category.setActive(false);
    }
}
