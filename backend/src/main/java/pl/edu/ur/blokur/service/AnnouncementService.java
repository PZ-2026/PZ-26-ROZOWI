package pl.edu.ur.blokur.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import pl.edu.ur.blokur.dto.AnnouncementDto;
import pl.edu.ur.blokur.models.Announcement;
import pl.edu.ur.blokur.models.Apartment;
import pl.edu.ur.blokur.models.Building;
import pl.edu.ur.blokur.models.Staircase;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.models.UserApartment;
import pl.edu.ur.blokur.repository.AnnouncementRepository;
import pl.edu.ur.blokur.repository.UserRepository;

/**
 * Serwis dostarczający logikę biznesową dla modułu ogłoszeń.
 * Pobiera ogłoszenia właściwe dla zalogowanego użytkownika
 * na podstawie jego powiązania z lokalem, klatką i budynkiem.
 */
@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;

    /**
     * Tworzy instancję serwisu z wymaganymi repozytoriami.
     *
     * @param announcementRepository repozytorium ogłoszeń
     * @param userRepository         repozytorium użytkowników
     */
    public AnnouncementService(AnnouncementRepository announcementRepository,
            UserRepository userRepository) {
        this.announcementRepository = announcementRepository;
        this.userRepository = userRepository;
    }

    /**
     * Zwraca listę ogłoszeń dopasowanych do zalogowanego użytkownika.
     * Uwzględnia ogłoszenia globalne oraz skierowane do budynku,
     * klatki lub mieszkania, do którego użytkownik jest przypisany.
     *
     * @param username adres email zalogowanego użytkownika (Subject JWT)
     * @return lista ogłoszeń w formie DTO
     */
    public List<AnnouncementDto> getAnnouncementsForUser(String username) {
        User user = userRepository.findByEmail(username).orElse(null);

        UUID buildingId = null;
        UUID staircaseId = null;
        UUID apartmentId = null;

        if (user != null && !user.getUserApartments().isEmpty()) {
            UserApartment ua = user.getUserApartments().get(0);
            Apartment apt = ua.getApartment();
            if (apt != null) {
                apartmentId = apt.getId();
                Staircase sc = apt.getStaircase();
                if (sc != null) {
                    staircaseId = sc.getId();
                    Building b = sc.getBuilding();
                    if (b != null) {
                        buildingId = b.getId();
                    }
                }
            }
        }

        List<Announcement> list = announcementRepository.findForUser(
                buildingId, staircaseId, apartmentId);

        return list.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    /**
     * Mapuje encję ogłoszenia na obiekt DTO.
     *
     * @param a encja ogłoszenia do zmapowania
     * @return obiekt DTO gotowy do serializacji
     */
    private AnnouncementDto mapToDto(Announcement a) {
        String authorName = "";
        if (a.getAuthor() != null) {
            authorName = a.getAuthor().getFirstName() + " "
                    + a.getAuthor().getLastName();
        }

        return new AnnouncementDto(
                a.getId(), a.getType(), a.getTitle(), a.getContent(),
                authorName, a.getPlannedDate(), a.getCreatedAt());
    }
}
