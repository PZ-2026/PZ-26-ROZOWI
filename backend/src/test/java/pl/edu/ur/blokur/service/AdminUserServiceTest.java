package pl.edu.ur.blokur.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.ur.blokur.dto.CreateUserRequest;
import pl.edu.ur.blokur.models.Apartment;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.repository.ApartmentRepository;
import pl.edu.ur.blokur.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminUserService — zarządzanie kontami użytkowników")
class AdminUserServiceTest {

    @Mock private UserRepository userRepository;

    @Mock private ApartmentRepository apartmentRepository;

    @Mock private PasswordResetService passwordResetService;

    @InjectMocks private AdminUserService adminUserService;

    private CreateUserRequest request;
    private Apartment apartment;
    private UUID apartmentId;

    @BeforeEach
    void setUp() {
        apartmentId = UUID.randomUUID();

        apartment = new Apartment();
        apartment.setId(apartmentId);
        apartment.setNumber("5");

        request = new CreateUserRequest();
        request.setFirstName("Jan");
        request.setLastName("Kowalski");
        request.setEmail("jan.kowalski@blokur.pl");
        request.setRole("MIESZKANIEC");
        request.setApartmentId(apartmentId);
    }

    // -------------------------------------------------------
    // createUser
    // -------------------------------------------------------

    @Nested
    @DisplayName("createUser — tworzenie nowego użytkownika")
    class CreateUser {

        @Test
        @DisplayName("Poprawne dane — zapisuje użytkownika z pustym hasłem")
        void shouldSaveUserWithEmptyPassword() {
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            adminUserService.createUser(request);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository, times(2)).save(captor.capture());
            assertThat(captor.getAllValues().get(0).getPasswordHash()).isEmpty();
        }

        @Test
        @DisplayName("Poprawne dane — zapisuje użytkownika z podanymi danymi")
        void shouldSaveUserWithCorrectFields() {
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            User result = adminUserService.createUser(request);

            assertThat(result.getFirstName()).isEqualTo("Jan");
            assertThat(result.getLastName()).isEqualTo("Kowalski");
            assertThat(result.getEmail()).isEqualTo("jan.kowalski@blokur.pl");
            assertThat(result.getRole()).isEqualTo("MIESZKANIEC");
            assertThat(result.isActive()).isTrue();
        }

        @Test
        @DisplayName("Poprawne dane — przypisuje użytkownika do lokalu")
        void shouldAssignUserToApartment() {
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            User result = adminUserService.createUser(request);

            assertThat(result.getUserApartments()).hasSize(1);
            assertThat(result.getUserApartments().get(0).getApartment()).isEqualTo(apartment);
        }

        @Test
        @DisplayName("Poprawne dane — wysyła zaproszenie e-mail")
        void shouldSendInvitationEmail() {
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            User result = adminUserService.createUser(request);

            verify(passwordResetService).inviteUser(result);
        }

        @Test
        @DisplayName("Email zajęty — rzuca IllegalArgumentException z informacją o emailu")
        void shouldThrowWhenEmailAlreadyExists() {
            when(userRepository.findByEmail(request.getEmail()))
                    .thenReturn(Optional.of(new User()));

            assertThatThrownBy(() -> adminUserService.createUser(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("email");
        }

        @Test
        @DisplayName("Email zajęty — nie zapisuje użytkownika")
        void shouldNotSaveUserWhenEmailTaken() {
            when(userRepository.findByEmail(request.getEmail()))
                    .thenReturn(Optional.of(new User()));

            assertThatThrownBy(() -> adminUserService.createUser(request));

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Nieistniejący lokal — rzuca IllegalArgumentException")
        void shouldThrowWhenApartmentNotFound() {
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adminUserService.createUser(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Lokal");
        }

        @Test
        @DisplayName("Nieistniejący lokal — nie zapisuje użytkownika")
        void shouldNotSaveUserWhenApartmentNotFound() {
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adminUserService.createUser(request));

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Nieistniejący lokal — nie wysyła zaproszenia")
        void shouldNotSendInvitationWhenApartmentNotFound() {
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adminUserService.createUser(request));

            verify(passwordResetService, never()).inviteUser(any());
        }
    }

    // -------------------------------------------------------
    // deleteUser
    // -------------------------------------------------------

    @Nested
    @DisplayName("deleteUser — soft delete użytkownika")
    class DeleteUser {

        @Test
        @DisplayName("Istniejący użytkownik — wywołuje delete na repozytorium")
        void shouldCallDeleteOnRepository() {
            UUID userId = UUID.randomUUID();
            User user = new User();
            user.setEmail("jan@blokur.pl");
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            adminUserService.deleteUser(userId);

            verify(userRepository).delete(user);
        }

        @Test
        @DisplayName("Nieistniejący użytkownik — rzuca IllegalArgumentException")
        void shouldThrowWhenUserNotFound() {
            UUID userId = UUID.randomUUID();
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adminUserService.deleteUser(userId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Użytkownik");
        }

        @Test
        @DisplayName("Nieistniejący użytkownik — nie wywołuje delete")
        void shouldNotCallDeleteWhenUserNotFound() {
            UUID userId = UUID.randomUUID();
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adminUserService.deleteUser(userId));

            verify(userRepository, never()).delete(any());
        }
    }
}
