package pl.edu.ur.blokur.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.ur.blokur.dto.CategoryRequest;
import pl.edu.ur.blokur.dto.CategoryResponse;
import pl.edu.ur.blokur.exception.NotFoundException;
import pl.edu.ur.blokur.models.TicketCategory;
import pl.edu.ur.blokur.repository.TicketCategoryRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TicketCategoryService — zarządzanie kategoriami zgłoszeń")
class TicketCategoryServiceTest {

    @Mock
    private TicketCategoryRepository categoryRepository;

    @InjectMocks
    private TicketCategoryService categoryService;

    private TicketCategory activeCategory;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();

        activeCategory = new TicketCategory();
        activeCategory.setId(categoryId);
        activeCategory.setName("Hydraulika");
        activeCategory.setActive(true);
    }

    @Nested
    @DisplayName("getActiveCategories")
    class GetActiveCategories {

        @Test
        @DisplayName("zwraca tylko aktywne kategorie")
        void returnsOnlyActiveCategories() {
            TicketCategory inactive = new TicketCategory();
            inactive.setId(UUID.randomUUID());
            inactive.setName("Stara kategoria");
            inactive.setActive(false);

            when(categoryRepository.findByIsActiveTrue()).thenReturn(List.of(activeCategory));

            List<CategoryResponse> result = categoryService.getActiveCategories();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Hydraulika");
        }

        @Test
        @DisplayName("zwraca pustą listę gdy brak aktywnych kategorii")
        void returnsEmptyListWhenNoneActive() {
            when(categoryRepository.findByIsActiveTrue()).thenReturn(List.of());

            List<CategoryResponse> result = categoryService.getActiveCategories();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("createCategory")
    class CreateCategory {

        @Test
        @DisplayName("zapisuje kategorię i zwraca odpowiedź")
        void savesAndReturnsCategory() {
            CategoryRequest request = new CategoryRequest();
            request.setName("Elektryka");

            TicketCategory saved = new TicketCategory();
            saved.setId(UUID.randomUUID());
            saved.setName("Elektryka");

            when(categoryRepository.save(any())).thenReturn(saved);

            CategoryResponse response = categoryService.createCategory(request);

            ArgumentCaptor<TicketCategory> captor = ArgumentCaptor.forClass(TicketCategory.class);
            verify(categoryRepository).save(captor.capture());
            assertThat(captor.getValue().getName()).isEqualTo("Elektryka");
            assertThat(captor.getValue().isActive()).isTrue();
            assertThat(response.getName()).isEqualTo("Elektryka");
        }
    }

    @Nested
    @DisplayName("updateCategory")
    class UpdateCategory {

        @Test
        @DisplayName("aktualizuje nazwę istniejącej kategorii")
        void updatesName() {
            CategoryRequest request = new CategoryRequest();
            request.setName("Nowa nazwa");

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(activeCategory));

            CategoryResponse response = categoryService.updateCategory(categoryId, request);

            assertThat(response.getName()).isEqualTo("Nowa nazwa");
            assertThat(activeCategory.getName()).isEqualTo("Nowa nazwa");
        }

        @Test
        @DisplayName("rzuca NotFoundException dla nieznanego id")
        void throwsNotFoundForUnknownId() {
            UUID unknownId = UUID.randomUUID();
            when(categoryRepository.findById(unknownId)).thenReturn(Optional.empty());

            CategoryRequest request = new CategoryRequest();
            request.setName("X");

            assertThatThrownBy(() -> categoryService.updateCategory(unknownId, request))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deactivateCategory")
    class DeactivateCategory {

        @Test
        @DisplayName("ustawia is_active = false")
        void setsIsActiveFalse() {
            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(activeCategory));

            categoryService.deactivateCategory(categoryId);

            assertThat(activeCategory.isActive()).isFalse();
        }

        @Test
        @DisplayName("rzuca NotFoundException dla nieznanego id")
        void throwsNotFoundForUnknownId() {
            UUID unknownId = UUID.randomUUID();
            when(categoryRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.deactivateCategory(unknownId))
                    .isInstanceOf(NotFoundException.class);
        }
    }
}
