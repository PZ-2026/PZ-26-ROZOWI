package pl.edu.ur.blokur.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;
import pl.edu.ur.blokur.dto.CastVoteRequest;
import pl.edu.ur.blokur.models.Resolution;
import pl.edu.ur.blokur.models.ResolutionOption;
import pl.edu.ur.blokur.models.ResolutionVote;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.repository.ResolutionOptionRepository;
import pl.edu.ur.blokur.repository.ResolutionRepository;
import pl.edu.ur.blokur.repository.ResolutionVoteRepository;
import pl.edu.ur.blokur.repository.UserRepository;

/**
 * Testy jednostkowe dla {@link ResolutionService}. Weryfikują logikę oddawania głosów w uchwałach,
 * w tym zabezpieczenie przed podwójnym głosowaniem za pomocą przechwytywania {@link
 * DataIntegrityViolationException}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ResolutionService — serwis uchwał i głosowań")
class ResolutionServiceTest {

    @Mock private ResolutionRepository resolutionRepository;

    @Mock private ResolutionOptionRepository resolutionOptionRepository;

    @Mock private ResolutionVoteRepository resolutionVoteRepository;

    @Mock private UserRepository userRepository;

    @InjectMocks private ResolutionService resolutionService;

    private static final String EMAIL = "lokator@blokur.pl";

    private UUID resolutionId;
    private UUID optionId;
    private Resolution resolution;
    private ResolutionOption option;
    private User voter;
    private CastVoteRequest request;

    @BeforeEach
    void setUp() {
        resolutionId = UUID.randomUUID();
        optionId = UUID.randomUUID();

        resolution = new Resolution();
        resolution.setId(resolutionId);
        resolution.setTitle("Uchwała nr 1/2026");
        resolution.setDescription("Opis uchwały testowej");

        option = new ResolutionOption();
        option.setId(optionId);
        option.setOptionText("Za");
        option.setResolution(resolution);

        voter = new User();
        voter.setId(UUID.randomUUID());
        voter.setEmail(EMAIL);
        voter.setFirstName("Jan");
        voter.setLastName("Testowy");

        request = new CastVoteRequest(optionId);
    }

    // =======================================================
    // Scenario: sukces — głos zapisany poprawnie
    // =======================================================

    @Nested
    @DisplayName("Poprawne oddanie głosu")
    class SuccessfulVoteTests {

        @Test
        @DisplayName("Zapisuje nowy rekord głosu w repozytorium")
        void shouldSaveVoteWhenAllDataIsValid() {
            when(resolutionRepository.findById(resolutionId)).thenReturn(Optional.of(resolution));
            when(resolutionOptionRepository.findById(optionId)).thenReturn(Optional.of(option));
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(voter));

            resolutionService.castVote(resolutionId, request, EMAIL);

            verify(resolutionVoteRepository).save(any(ResolutionVote.class));
        }

        @Test
        @DisplayName("Głos zapisywany jest z poprawnymi danymi uchwały, opcji i głosującego")
        void shouldSaveVoteWithCorrectEntities() {
            when(resolutionRepository.findById(resolutionId)).thenReturn(Optional.of(resolution));
            when(resolutionOptionRepository.findById(optionId)).thenReturn(Optional.of(option));
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(voter));

            resolutionService.castVote(resolutionId, request, EMAIL);

            verify(resolutionVoteRepository).save(any(ResolutionVote.class));
            verify(resolutionRepository).findById(resolutionId);
            verify(resolutionOptionRepository).findById(optionId);
            verify(userRepository).findByEmail(EMAIL);
        }
    }

    // =======================================================
    // Scenario: podwójne głosowanie — konflikt unikalności
    // =======================================================

    @Nested
    @DisplayName("Podwójne głosowanie — ochrona UNIQUE (resolution_id, voter_id)")
    class DuplicateVoteTests {

        @Test
        @DisplayName(
                "Rzuca ResponseStatusException z HTTP 409 gdy użytkownik głosował już wcześniej")
        void shouldThrow409WhenUserAlreadyVoted() {
            when(resolutionRepository.findById(resolutionId)).thenReturn(Optional.of(resolution));
            when(resolutionOptionRepository.findById(optionId)).thenReturn(Optional.of(option));
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(voter));
            when(resolutionVoteRepository.save(any(ResolutionVote.class)))
                    .thenThrow(
                            new DataIntegrityViolationException(
                                    "unique constraint violation:"
                                            + " resolution_votes_resolution_id_voter_id_key"));

            assertThatThrownBy(() -> resolutionService.castVote(resolutionId, request, EMAIL))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(
                            ex -> {
                                ResponseStatusException rse = (ResponseStatusException) ex;
                                assertThat(rse.getStatusCode()).isEqualTo(CONFLICT);
                            });
        }

        @Test
        @DisplayName("Komunikat błędu 409 informuje o wcześniejszym oddaniu głosu")
        void shouldIncludeInformativeMessageIn409Response() {
            when(resolutionRepository.findById(resolutionId)).thenReturn(Optional.of(resolution));
            when(resolutionOptionRepository.findById(optionId)).thenReturn(Optional.of(option));
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(voter));
            when(resolutionVoteRepository.save(any(ResolutionVote.class)))
                    .thenThrow(new DataIntegrityViolationException("duplicate key"));

            assertThatThrownBy(() -> resolutionService.castVote(resolutionId, request, EMAIL))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Użytkownik oddał już głos w tej uchwale");
        }
    }

    // =======================================================
    // Scenario: brak zasobu w bazie danych
    // =======================================================

    @Nested
    @DisplayName("Brakujące zasoby — HTTP 404")
    class NotFoundTests {

        @Test
        @DisplayName("Rzuca ResponseStatusException z HTTP 404 gdy uchwała nie istnieje")
        void shouldThrow404WhenResolutionNotFound() {
            when(resolutionRepository.findById(resolutionId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> resolutionService.castVote(resolutionId, request, EMAIL))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(
                            ex -> {
                                ResponseStatusException rse = (ResponseStatusException) ex;
                                assertThat(rse.getStatusCode()).isEqualTo(NOT_FOUND);
                            });

            verify(resolutionVoteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Rzuca ResponseStatusException z HTTP 404 gdy opcja głosowania nie istnieje")
        void shouldThrow404WhenOptionNotFound() {
            when(resolutionRepository.findById(resolutionId)).thenReturn(Optional.of(resolution));
            when(resolutionOptionRepository.findById(optionId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> resolutionService.castVote(resolutionId, request, EMAIL))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(
                            ex -> {
                                ResponseStatusException rse = (ResponseStatusException) ex;
                                assertThat(rse.getStatusCode()).isEqualTo(NOT_FOUND);
                            });

            verify(resolutionVoteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Rzuca ResponseStatusException z HTTP 404 gdy użytkownik nie istnieje")
        void shouldThrow404WhenVoterNotFound() {
            when(resolutionRepository.findById(resolutionId)).thenReturn(Optional.of(resolution));
            when(resolutionOptionRepository.findById(optionId)).thenReturn(Optional.of(option));
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> resolutionService.castVote(resolutionId, request, EMAIL))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(
                            ex -> {
                                ResponseStatusException rse = (ResponseStatusException) ex;
                                assertThat(rse.getStatusCode()).isEqualTo(NOT_FOUND);
                            });

            verify(resolutionVoteRepository, never()).save(any());
        }
    }

    // =======================================================
    // Scenario: opcja nie należy do wskazanej uchwały
    // =======================================================

    @Nested
    @DisplayName("Walidacja przynależności opcji do uchwały — HTTP 400")
    class OptionMismatchTests {

        @Test
        @DisplayName("Rzuca ResponseStatusException z HTTP 400 gdy opcja należy do innej uchwały")
        void shouldThrow400WhenOptionDoesNotBelongToResolution() {
            Resolution otherResolution = new Resolution();
            otherResolution.setId(UUID.randomUUID());
            otherResolution.setTitle("Inna uchwała");

            option.setResolution(otherResolution);

            when(resolutionRepository.findById(resolutionId)).thenReturn(Optional.of(resolution));
            when(resolutionOptionRepository.findById(optionId)).thenReturn(Optional.of(option));

            assertThatThrownBy(() -> resolutionService.castVote(resolutionId, request, EMAIL))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(
                            ex -> {
                                ResponseStatusException rse = (ResponseStatusException) ex;
                                assertThat(rse.getStatusCode()).isEqualTo(BAD_REQUEST);
                            });

            verify(resolutionVoteRepository, never()).save(any());
        }
    }
}
