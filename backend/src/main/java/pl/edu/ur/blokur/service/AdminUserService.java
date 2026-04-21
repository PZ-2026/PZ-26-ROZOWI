package pl.edu.ur.blokur.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.ur.blokur.dto.CreateUserRequest;
import pl.edu.ur.blokur.models.Apartment;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.models.UserApartment;
import pl.edu.ur.blokur.repository.ApartmentRepository;
import pl.edu.ur.blokur.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final ApartmentRepository apartmentRepository;
    private final PasswordResetService passwordResetService;

    public AdminUserService(UserRepository userRepository, ApartmentRepository apartmentRepository,
                            PasswordResetService passwordResetService) {
        this.userRepository = userRepository;
        this.apartmentRepository = apartmentRepository;
        this.passwordResetService = passwordResetService;
    }

    @Transactional
    public User createUser(CreateUserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Podany adres email jest już zajęty.");
        }

        Apartment apartment = apartmentRepository.findById(request.getApartmentId())
            .orElseThrow(() -> new IllegalArgumentException("Lokal o podanym ID nie istnieje."));

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
        passwordResetService.inviteUser(finalUser);
        return finalUser;
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Użytkownik o podanym ID nie istnieje."));
        userRepository.delete(user);
    }
}
