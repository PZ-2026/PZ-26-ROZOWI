package pl.edu.ur.blokur.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
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
import pl.edu.ur.blokur.dto.CreateResolutionRequest;
import pl.edu.ur.blokur.dto.ResolutionDetailDto;
import pl.edu.ur.blokur.models.Building;
import pl.edu.ur.blokur.models.Resolution;
import pl.edu.ur.blokur.models.ResolutionOption;
import pl.edu.ur.blokur.models.ResolutionVote;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.repository.BuildingRepository;
import pl.edu.ur.blokur.repository.ResolutionOptionRepository;
import pl.edu.ur.blokur.repository.ResolutionRepository;
import pl.edu.ur.blokur.repository.ResolutionVoteRepository;
import pl.edu.ur.blokur.repository.UserRepository;

/**
 * Testy jednostkowe dla {@link ResolutionService}. Weryfikują logikę oddawania głosów w uchwałach,
 * tworzenia nowych uchwał, zarządzania wynikami i generowania raportów.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ResolutionService — serwis uchwał i głosowań")
class ResolutionServiceTest {

    @Mock private ResolutionRepository resolutionRepository;

    @Mock private ResolutionOptionRepository resolutionOptionRepository;

    @Mock private ResolutionVoteRepository resolutionVoteRepository;

    @Mock private UserRepository userRepository;

    @Mock private BuildingRepository buildingRepository;

    @InjectMocks private ResolutionService resolutionService;

    private static final String EMAIL = "lokator@blokur.pl";
    private static final String ZARZADCA_EMAIL = "zarzadca@blokur.pl";

    private UUID resolutionId;
    private UUID optionId;
    private UUID buildingId;
    private Resolution resolution;
    private ResolutionOption option;
    private User voter;
    private User zarzadca;
    private Building building;
    private CastVoteRequest request;

    @BeforeEach
    void setUp() {
        resolutionId = UUID.randomUUID();
        optionId = UUID.randomUUID();
        buildingId = UUID.randomUUID();

        building = new Building();
        building.setId(buildingId);

        resolution = new Resolution();
        resolution.setId(resolutionId);
        resolution.setTitle("Uchwała nr 1/2026");
        resolution.setDescription("Opis uchwały testowej");
        resolution.setBuilding(building);
        resolution.setEndDate(LocalDateTime.now().plusDays(5));

        option = new ResolutionOption();
        option.setId(optionId);
        option.setOptionText("Za");
        option.setResolution(resolution);

        voter = new User();
        voter.setId(UUID.randomUUID());
        voter.setEmail(EMAIL);
        voter.setRole("MIESZKANIEC");
        voter.setFirstName("Jan");
        voter.setLastName("Testowy");

        zarzadca = new User();
        zarzadca.setId(UUID.randomUUID());
        zarzadca.setEmail(ZARZADCA_EMAIL);
        zarzadca.setRole("ZARZADCA");
        zarzadca.setFirstName("Piotr");
        zarzadca.setLastName("Szef");

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
    }

    // =======================================================
    // Scenario: Tworzenie uchwały
    // =======================================================

    @Nested
    @DisplayName("Tworzenie uchwały przez Zarządcę")
    class CreateResolutionTests {

        @Test
        @DisplayName("Zarządca może poprawnie utworzyć nową uchwałę")
        void zarzadcaShouldCreateResolution() {
            CreateResolutionRequest createReq = new CreateResolutionRequest();
            createReq.setTitle("Nowa uchwała");
            createReq.setDescription("Opis");
            createReq.setEndDate(LocalDateTime.now().plusDays(7));
            createReq.setTargetBuildingId(buildingId);
            createReq.setOptions(Arrays.asList("Opcja 1", "Opcja 2"));

            when(userRepository.findByEmail(ZARZADCA_EMAIL)).thenReturn(Optional.of(zarzadca));
            when(buildingRepository.findById(buildingId)).thenReturn(Optional.of(building));
            when(resolutionRepository.save(any(Resolution.class))).thenReturn(resolution);

            resolutionService.createResolution(createReq, ZARZADCA_EMAIL);

            verify(resolutionRepository).save(any(Resolution.class));
            verify(resolutionOptionRepository, org.mockito.Mockito.times(2))
                    .save(any(ResolutionOption.class));
        }

        @Test
        @DisplayName("Mieszkaniec nie może tworzyć uchwały (rzuca 403 FORBIDDEN)")
        void mieszkaniecShouldNotCreateResolution() {
            CreateResolutionRequest createReq = new CreateResolutionRequest();
            createReq.setTitle("Nowa uchwała");
            createReq.setDescription("Opis");
            createReq.setEndDate(LocalDateTime.now().plusDays(7));
            createReq.setTargetBuildingId(buildingId);
            createReq.setOptions(Arrays.asList("Opcja 1", "Opcja 2"));

            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(voter));

            assertThatThrownBy(() -> resolutionService.createResolution(createReq, EMAIL))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(
                            ex -> {
                                ResponseStatusException rse = (ResponseStatusException) ex;
                                assertThat(rse.getStatusCode()).isEqualTo(FORBIDDEN);
                            });

            verify(resolutionRepository, never()).save(any(Resolution.class));
        }
    }

    // =======================================================
    // Scenario: Widoczność wyników
    // =======================================================

    @Nested
    @DisplayName("Pobieranie szczegółów i wyników uchwały")
    class ResolutionDetailsTests {

        @Test
        @DisplayName("Zarządca widzi wyniki nawet w trakcie trwania głosowania")
        void zarzadcaSeesResultsAlways() {
            when(userRepository.findByEmail(ZARZADCA_EMAIL)).thenReturn(Optional.of(zarzadca));
            when(resolutionRepository.findById(resolutionId)).thenReturn(Optional.of(resolution));
            when(resolutionOptionRepository.findByResolutionId(resolutionId))
                    .thenReturn(List.of(option));
            when(resolutionVoteRepository.existsByResolutionIdAndVoterId(
                            resolutionId, zarzadca.getId()))
                    .thenReturn(false);
            when(resolutionVoteRepository.countByOptionId(optionId)).thenReturn(5L);

            ResolutionDetailDto dto =
                    resolutionService.getResolutionDetails(resolutionId, ZARZADCA_EMAIL);

            assertThat(dto.getResults()).isNotNull();
            assertThat(dto.getResults()).hasSize(1);
            assertThat(dto.getResults().get(0).getVotesCount()).isEqualTo(5L);
        }
    }
}
