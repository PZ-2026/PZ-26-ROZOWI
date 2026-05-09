package pl.edu.ur.blokur.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.ur.blokur.dto.CreateUserRequest;
import pl.edu.ur.blokur.dto.UpdateUserRequest;
import pl.edu.ur.blokur.dto.UserResponse;
import pl.edu.ur.blokur.models.Apartment;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.models.UserApartment;
import pl.edu.ur.blokur.repository.ApartmentRepository;
import pl.edu.ur.blokur.repository.UserRepository;

/**
 * Serwis obsługujący administracyjne operacje na użytkownikach wykonywane przez zarządcę:
 * tworzenie, listowanie, edycję, dezaktywację i usuwanie kont.
 */
@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final ApartmentRepository apartmentRepository;
    private final InvitationService invitationService;

    /**
     * Tworzy serwis z wymaganymi zależnościami.
     *
     * @param userRepository repozytorium użytkowników
     * @param apartmentRepository repozytorium mieszkań
     * @param invitationService serwis do wysyłki zaproszeń z linkiem do ustawienia hasła (72 h)
     */
    public AdminUserService(
            UserRepository userRepository,
            ApartmentRepository apartmentRepository,
            InvitationService invitationService) {
        this.userRepository = userRepository;
        this.apartmentRepository = apartmentRepository;
        this.invitationService = invitationService;
    }

    /**
     * Zwraca listę wszystkich użytkowników (nieusuniętych) wraz z rolą, lokalem i statusem
     * aktywności.
     *
     * @return lista DTO użytkowników
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(
                        user -> {
                            UUID apartmentId =
                                    user.getUserApartments().isEmpty()
                                            ? null
                                            : user.getUserApartments()
                                                    .get(0)
                                                    .getApartment()
                                                    .getId();
                            return new UserResponse(
                                    user.getId(),
                                    user.getFirstName(),
                                    user.getLastName(),
                                    user.getEmail(),
                                    user.getPhone(),
                                    user.getRole(),
                                    user.isActive(),
                                    user.getCreatedAt(),
                                    apartmentId);
                        })
                .toList();
    }

    /**
     * Tworzy nowego użytkownika z pustym hasłem i wysyła mu e-mail z linkiem do ustawienia hasła.
     *
     * @param request dane nowego użytkownika (imię, email, rola, id lokalu)
     * @return utworzony użytkownik po zapisie w bazie
     * @throws IllegalArgumentException jeśli email jest zajęty lub lokal nie istnieje
     */
    @Transactional
    public User createUser(CreateUserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Podany adres email jest już zajęty.");
        }

        Apartment apartment =
                apartmentRepository
                        .findById(request.getApartmentId())
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Lokal o podanym ID nie istnieje."));

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setPasswordHash("");
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        UserApartment userApartment = new UserApartment();
        userApartment.setUser(savedUser);
        userApartment.setApartment(apartment);
        savedUser.getUserApartments().add(userApartment);

        User finalUser = userRepository.save(savedUser);
        invitationService.inviteUser(finalUser);
        return finalUser;
    }

    /**
     * Aktualizuje dane użytkownika: imię, nazwisko, telefon, rolę oraz przypisany lokal.
     *
     * <p>Jeśli {@code request.getApartmentId()} jest {@code null}, dotychczasowe przypisania do
     * lokali pozostają bez zmian. W przeciwnym razie stare przypisania są zastępowane nowym.
     *
     * @param id identyfikator użytkownika do edycji
     * @param request nowe dane użytkownika
     * @return zaktualizowany użytkownik
     * @throws IllegalArgumentException jeśli użytkownik nie istnieje lub lokal nie istnieje
     */
    @Transactional
    public User updateUser(UUID id, UpdateUserRequest request) {
        User user =
                userRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Użytkownik o podanym ID nie istnieje."));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());

        if (request.getApartmentId() != null) {
            Apartment apartment =
                    apartmentRepository
                            .findById(request.getApartmentId())
                            .orElseThrow(
                                    () ->
                                            new IllegalArgumentException(
                                                    "Lokal o podanym ID nie istnieje."));
            user.getUserApartments().clear();
            UserApartment userApartment = new UserApartment();
            userApartment.setUser(user);
            userApartment.setApartment(apartment);
            user.getUserApartments().add(userApartment);
        }

        return userRepository.save(user);
    }

    /**
     * Dezaktywuje konto użytkownika ustawiając flagę {@code is_active = false}. Historia zgłoszeń i
     * rozliczeń powiązana z kontem zostaje zachowana.
     *
     * @param id identyfikator użytkownika do dezaktywacji
     * @throws IllegalArgumentException jeśli użytkownik nie istnieje
     */
    @Transactional
    public void deactivateUser(UUID id) {
        User user =
                userRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Użytkownik o podanym ID nie istnieje."));
        user.setActive(false);
        userRepository.save(user);
    }

    /**
     * Usuwa użytkownika o podanym identyfikatorze (soft delete — rekord zostaje w bazie z flagą
     * {@code deleted = true}).
     *
     * @param id identyfikator użytkownika
     * @throws IllegalArgumentException jeśli użytkownik nie istnieje
     */
    @Transactional
    public void deleteUser(UUID id) {
        User user =
                userRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Użytkownik o podanym ID nie istnieje."));
        userRepository.delete(user);
    }
}
