package pl.edu.ur.blokur.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.ur.blokur.dto.UserWithTicketsDto;
import pl.edu.ur.blokur.repository.UserRepository;

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
     * Zwraca profil zalogowanego użytkownika na podstawie jego adresu e-mail.
     *
     * @param email adres e-mail użytkownika
     * @return DTO z profilem użytkownika
     */
    @Transactional(readOnly = true)
    public pl.edu.ur.blokur.dto.UserResponse getMe(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new pl.edu.ur.blokur.exception.NotFoundException("Użytkownik o podanym adresie email nie istnieje"));
        var apartmentId = user.getUserApartments().isEmpty() 
                ? null 
                : user.getUserApartments().get(0).getApartment().getId();
        return new pl.edu.ur.blokur.dto.UserResponse(
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
