package pl.edu.ur.blokur.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.ur.blokur.dto.UserResponse;
import pl.edu.ur.blokur.dto.UserWithTicketsDto;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.repository.UserRepository;

import java.util.UUID;

/** Serwis zarządzający logiką użytkowników (odczyt). */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Zwraca listę użytkowników o określonej roli, wraz z liczbą aktywnych zgłoszeń.
     *
     * @param role rola użytkownika (np. KONSERWATOR)
     * @return lista DTO
     */
    @Transactional(readOnly = true)
    public List<UserWithTicketsDto> getUsersWithActiveTicketCountByRole(String role) {
        return userRepository.findUsersWithActiveTicketsByRole(role);
    }

    /**
     * Zwraca profil użytkownika na podstawie jego adresu e-mail.
     *
     * @param email adres e-mail użytkownika
     * @return UserResponse z danymi profilu
     */
    @Transactional(readOnly = true)
    public UserResponse getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono użytkownika"));
        
        UUID apartmentId = user.getUserApartments().isEmpty()
                ? null
                : user.getUserApartments().get(0).getApartment().getId();
                
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt(),
                apartmentId
        );
    }
}
