package pl.edu.ur.blokur.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.edu.ur.blokur.dto.CastVoteRequest;
import pl.edu.ur.blokur.dto.CreateResolutionRequest;
import pl.edu.ur.blokur.dto.ResolutionDetailDto;
import pl.edu.ur.blokur.dto.ResolutionDto;
import pl.edu.ur.blokur.dto.ResolutionOptionDto;
import pl.edu.ur.blokur.dto.ResolutionOptionResultDto;
import pl.edu.ur.blokur.models.Building;
import pl.edu.ur.blokur.models.Resolution;
import pl.edu.ur.blokur.models.ResolutionOption;
import pl.edu.ur.blokur.models.ResolutionVote;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.models.UserApartment;
import pl.edu.ur.blokur.repository.BuildingRepository;
import pl.edu.ur.blokur.repository.ResolutionOptionRepository;
import pl.edu.ur.blokur.repository.ResolutionRepository;
import pl.edu.ur.blokur.repository.ResolutionVoteRepository;
import pl.edu.ur.blokur.repository.UserRepository;

/**
 * Serwis dostarczający logikę biznesową dla modułu uchwał i głosowań. Odpowiada za walidację danych
 * wejściowych, sprawdzenie stanu uchwały oraz zapis oddanego głosu z zabezpieczeniem przed
 * podwójnym głosowaniem, a także zarządza cyklem życia uchwały (tworzenie, pobieranie wyników).
 */
@Service
public class ResolutionService {

    private final ResolutionRepository resolutionRepository;
    private final ResolutionOptionRepository resolutionOptionRepository;
    private final ResolutionVoteRepository resolutionVoteRepository;
    private final UserRepository userRepository;
    private final BuildingRepository buildingRepository;

    /**
     * Tworzy instancję serwisu z wymaganymi repozytoriami.
     *
     * @param resolutionRepository repozytorium uchwał
     * @param resolutionOptionRepository repozytorium opcji głosowania
     * @param resolutionVoteRepository repozytorium oddanych głosów
     * @param userRepository repozytorium użytkowników
     * @param buildingRepository repozytorium budynków
     */
    public ResolutionService(
            ResolutionRepository resolutionRepository,
            ResolutionOptionRepository resolutionOptionRepository,
            ResolutionVoteRepository resolutionVoteRepository,
            UserRepository userRepository,
            BuildingRepository buildingRepository) {
        this.resolutionRepository = resolutionRepository;
        this.resolutionOptionRepository = resolutionOptionRepository;
        this.resolutionVoteRepository = resolutionVoteRepository;
        this.userRepository = userRepository;
        this.buildingRepository = buildingRepository;
    }

    /**
     * Rejestruje głos zalogowanego użytkownika na wybraną opcję w uchwale.
     *
     * @param resolutionId identyfikator UUID uchwały, w której oddawany jest głos
     * @param request obiekt DTO zawierający {@code optionId} wybranej opcji
     * @param username adres e-mail zalogowanego użytkownika (Subject z JWT)
     * @throws ResponseStatusException z kodem 404 gdy uchwała lub opcja nie istnieje, z kodem 409
     *     gdy użytkownik już oddał głos w tej uchwale, z kodem 400 gdy wybrana opcja nie należy do
     *     wskazanej uchwały
     */
    public void castVote(UUID resolutionId, CastVoteRequest request, String username) {
        Resolution resolution =
                resolutionRepository
                        .findById(resolutionId)
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Uchwała o podanym identyfikatorze nie istnieje."));

        ResolutionOption option =
                resolutionOptionRepository
                        .findById(request.getOptionId())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Opcja głosowania nie istnieje."));

        if (!option.getResolution().getId().equals(resolutionId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Wybrana opcja nie należy do wskazanej uchwały.");
        }

        User voter =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Użytkownik nie został odnaleziony."));

        ResolutionVote vote = new ResolutionVote();
        vote.setResolution(resolution);
        vote.setOption(option);
        vote.setVoter(voter);

        try {
            resolutionVoteRepository.save(vote);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Użytkownik oddał już głos w tej uchwale.");
        }
    }

    /**
     * Tworzy nowe głosowanie w systemie na podstawie przesłanego żądania. Wymaga roli ZARZADCA.
     *
     * @param request dane nowej uchwały
     * @param username adres e-mail twórcy uchwały
     */
    public void createResolution(CreateResolutionRequest request, String username) {
        User user =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Użytkownik nie istnieje."));

        if (!"ZARZADCA".equals(user.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Tylko zarządca może tworzyć uchwały.");
        }

        Building building =
                buildingRepository
                        .findById(request.getTargetBuildingId())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Budynek nie istnieje."));

        Resolution resolution = new Resolution();
        resolution.setTitle(request.getTitle());
        resolution.setDescription(request.getDescription());
        resolution.setEndDate(request.getEndDate());
        resolution.setAuthor(user);
        resolution.setBuilding(building);

        resolution = resolutionRepository.save(resolution);

        for (String optionText : request.getOptions()) {
            ResolutionOption option = new ResolutionOption();
            option.setResolution(resolution);
            option.setOptionText(optionText);
            resolutionOptionRepository.save(option);
        }
    }

    /**
     * Zwraca listę uchwał dostępnych dla użytkownika. Mieszkaniec widzi uchwały powiązane z jego
     * budynkiem, natomiast Zarządca widzi wszystkie w systemie.
     *
     * @param username adres e-mail użytkownika
     * @return lista DTO uchwał
     */
    public List<ResolutionDto> getResolutionsForUser(String username) {
        User user =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Użytkownik nie istnieje."));

        List<Resolution> resolutions = new ArrayList<>();

        if ("ZARZADCA".equals(user.getRole())) {
            resolutions = resolutionRepository.findAll();
        } else {
            UUID buildingId = null;
            if (user.getUserApartments() != null && !user.getUserApartments().isEmpty()) {
                UserApartment ua = user.getUserApartments().get(0);
                if (ua.getApartment() != null
                        && ua.getApartment().getStaircase() != null
                        && ua.getApartment().getStaircase().getBuilding() != null) {
                    buildingId = ua.getApartment().getStaircase().getBuilding().getId();
                }
            }
            if (buildingId != null) {
                resolutions = resolutionRepository.findByBuildingId(buildingId);
            }
        }

        return resolutions.stream()
                .map(
                        r -> {
                            String authorName =
                                    r.getAuthor() != null
                                            ? r.getAuthor().getFirstName()
                                                    + " "
                                                    + r.getAuthor().getLastName()
                                            : "";
                            return new ResolutionDto(
                                    r.getId(),
                                    r.getTitle(),
                                    r.getDescription(),
                                    r.getEndDate(),
                                    r.getBuilding().getId(),
                                    authorName);
                        })
                .collect(Collectors.toList());
    }

    /**
     * Pobiera szczegóły uchwały wraz z dostępnymi opcjami. Wyniki głosowania (liczba głosów) są
     * dołączane tylko, jeśli pytający jest Zarządcą, termin głosowania minął lub użytkownik już
     * oddał głos.
     *
     * @param resolutionId identyfikator uchwały
     * @param username adres e-mail użytkownika
     * @return szczegóły uchwały w postaci DTO
     */
    public ResolutionDetailDto getResolutionDetails(UUID resolutionId, String username) {
        User user =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Użytkownik nie istnieje."));

        Resolution resolution =
                resolutionRepository
                        .findById(resolutionId)
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Uchwała nie istnieje."));

        if (!"ZARZADCA".equals(user.getRole())) {
            UUID buildingId = null;
            if (user.getUserApartments() != null && !user.getUserApartments().isEmpty()) {
                UserApartment ua = user.getUserApartments().get(0);
                if (ua.getApartment() != null
                        && ua.getApartment().getStaircase() != null
                        && ua.getApartment().getStaircase().getBuilding() != null) {
                    buildingId = ua.getApartment().getStaircase().getBuilding().getId();
                }
            }
            if (buildingId == null || !buildingId.equals(resolution.getBuilding().getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Brak dostępu do uchwały.");
            }
        }

        List<ResolutionOption> optionsEntities =
                resolutionOptionRepository.findByResolutionId(resolutionId);
        List<ResolutionOptionDto> options =
                optionsEntities.stream()
                        .map(o -> new ResolutionOptionDto(o.getId(), o.getOptionText()))
                        .collect(Collectors.toList());

        boolean hasVoted =
                resolutionVoteRepository.existsByResolutionIdAndVoterId(resolutionId, user.getId());
        boolean endDatePassed = LocalDateTime.now().isAfter(resolution.getEndDate());
        boolean isZarzadca = "ZARZADCA".equals(user.getRole());

        List<ResolutionOptionResultDto> results = null;

        if (isZarzadca || endDatePassed || hasVoted) {
            results =
                    optionsEntities.stream()
                            .map(
                                    o ->
                                            new ResolutionOptionResultDto(
                                                    o.getId(),
                                                    o.getOptionText(),
                                                    resolutionVoteRepository.countByOptionId(
                                                            o.getId())))
                            .collect(Collectors.toList());
        }

        String authorName =
                resolution.getAuthor() != null
                        ? resolution.getAuthor().getFirstName()
                                + " "
                                + resolution.getAuthor().getLastName()
                        : "";

        return new ResolutionDetailDto(
                resolution.getId(),
                resolution.getTitle(),
                resolution.getDescription(),
                resolution.getEndDate(),
                resolution.getBuilding().getId(),
                authorName,
                options,
                results);
    }

    /**
     * Generuje plik PDF z raportem z głosowania, zawierającym listę opcji oraz oddane na nie głosy.
     * Funkcja dostępna tylko dla Zarządcy i tylko po zakończeniu głosowania.
     *
     * @param resolutionId identyfikator uchwały
     * @param username adres e-mail użytkownika
     * @return tablica bajtów reprezentująca plik PDF
     */
    public byte[] generateResolutionReport(UUID resolutionId, String username) {
        User user =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Użytkownik nie istnieje."));

        if (!"ZARZADCA".equals(user.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Tylko zarządca może generować raporty.");
        }

        Resolution resolution =
                resolutionRepository
                        .findById(resolutionId)
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Uchwała nie istnieje."));

        if (LocalDateTime.now().isBefore(resolution.getEndDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Głosowanie jeszcze się nie zakończyło.");
        }

        List<ResolutionOption> optionsEntities =
                resolutionOptionRepository.findByResolutionId(resolutionId);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Raport z wynikow glosowania").setBold().setFontSize(16));
            document.add(new Paragraph("Tytul: " + resolution.getTitle()));
            document.add(new Paragraph("Data zakonczenia: " + resolution.getEndDate().toString()));
            document.add(new Paragraph(" "));

            Table table = new Table(2);
            table.addHeaderCell("Opcja");
            table.addHeaderCell("Liczba glosow");

            long totalVotes = 0;
            for (ResolutionOption option : optionsEntities) {
                long votesCount = resolutionVoteRepository.countByOptionId(option.getId());
                table.addCell(option.getOptionText());
                table.addCell(String.valueOf(votesCount));
                totalVotes += votesCount;
            }

            document.add(table);
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Suma oddanych glosow: " + totalVotes).setBold());

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Błąd podczas generowania raportu PDF.");
        }
    }
}
