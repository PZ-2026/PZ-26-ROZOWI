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
}
