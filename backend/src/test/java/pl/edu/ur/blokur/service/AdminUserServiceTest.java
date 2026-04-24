package pl.edu.ur.blokur.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
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
import pl.edu.ur.blokur.dto.UpdateUserRequest;
import pl.edu.ur.blokur.dto.UserResponse;
import pl.edu.ur.blokur.models.Apartment;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.models.UserApartment;
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
    // getAllUsers
    // -------------------------------------------------------

    @Nested
    @DisplayName("getAllUsers — lista użytkowników")
    class GetAllUsers {

        @Test
        @DisplayName("Brak użytkowników — zwraca pustą listę")
        void shouldReturnEmptyListWhenNoUsers() {
            when(userRepository.findAll()).thenReturn(List.of());

            List<UserResponse> result = adminUserService.getAllUsers();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Użytkownicy bez lokalu — apartmentId jest null")
        void shouldReturnNullApartmentIdWhenNoApartmentAssigned() {
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setFirstName("Anna");
            user.setLastName("Nowak");
            user.setEmail("anna@blokur.pl");
            user.setRole("MIESZKANIEC");
            user.setActive(true);

            when(userRepository.findAll()).thenReturn(List.of(user));

            List<UserResponse> result = adminUserService.getAllUsers();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getApartmentId()).isNull();
        }

        @Test
        @DisplayName("Użytkownik z lokalem — zwraca poprawne apartmentId")
        void shouldReturnApartmentIdWhenAssigned() {
            UUID userId = UUID.randomUUID();
            User user = new User();
            user.setId(userId);
            user.setFirstName("Jan");
            user.setLastName("Kowalski");
            user.setEmail("jan@blokur.pl");
            user.setRole("MIESZKANIEC");
            user.setActive(true);

            UserApartment ua = new UserApartment();
            ua.setApartment(apartment);
            ua.setUser(user);
            user.getUserApartments().add(ua);

            when(userRepository.findAll()).thenReturn(List.of(user));

            List<UserResponse> result = adminUserService.getAllUsers();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getApartmentId()).isEqualTo(apartmentId);
        }

        @Test
        @DisplayName("Użytkownik z telefonem — zwraca poprawny numer telefonu")
        void shouldReturnPhoneNumber() {
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setFirstName("Jan");
            user.setLastName("Kowalski");
            user.setEmail("jan@blokur.pl");
            user.setRole("MIESZKANIEC");
            user.setActive(true);
            user.setPhone("500600700");

            when(userRepository.findAll()).thenReturn(List.of(user));

            List<UserResponse> result = adminUserService.getAllUsers();

            assertThat(result.get(0).getPhone()).isEqualTo("500600700");
        }

        @Test
        @DisplayName("Wielu użytkowników — zwraca pełną listę")
        void shouldReturnAllUsers() {
            User u1 = new User();
            u1.setId(UUID.randomUUID());
            u1.setFirstName("Jan");
            u1.setLastName("Kowalski");
            u1.setEmail("jan@blokur.pl");
            u1.setRole("MIESZKANIEC");
            u1.setActive(true);

            User u2 = new User();
            u2.setId(UUID.randomUUID());
            u2.setFirstName("Anna");
            u2.setLastName("Nowak");
            u2.setEmail("anna@blokur.pl");
            u2.setRole("ZARZADCA");
            u2.setActive(false);

            when(userRepository.findAll()).thenReturn(List.of(u1, u2));

            List<UserResponse> result = adminUserService.getAllUsers();

            assertThat(result).hasSize(2);
        }
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
    // updateUser
    // -------------------------------------------------------

    @Nested
    @DisplayName("updateUser — edycja danych użytkownika")
    class UpdateUser {

        private UUID userId;
        private User existingUser;
        private UpdateUserRequest updateRequest;

        @BeforeEach
        void setUpUpdate() {
            userId = UUID.randomUUID();

            existingUser = new User();
            existingUser.setId(userId);
            existingUser.setFirstName("Jan");
            existingUser.setLastName("Kowalski");
            existingUser.setEmail("jan@blokur.pl");
            existingUser.setRole("MIESZKANIEC");
            existingUser.setActive(true);

            updateRequest = new UpdateUserRequest();
            updateRequest.setFirstName("Janusz");
            updateRequest.setLastName("Nowak");
            updateRequest.setPhone("500600700");
            updateRequest.setRole("ZARZADCA");
        }

        @Test
        @DisplayName("Poprawne dane bez zmiany lokalu — aktualizuje imię, nazwisko, telefon i rolę")
        void shouldUpdateBasicFields() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            User result = adminUserService.updateUser(userId, updateRequest);

            assertThat(result.getFirstName()).isEqualTo("Janusz");
            assertThat(result.getLastName()).isEqualTo("Nowak");
            assertThat(result.getPhone()).isEqualTo("500600700");
            assertThat(result.getRole()).isEqualTo("ZARZADCA");
        }

        @Test
        @DisplayName("Poprawne dane bez zmiany lokalu — nie modyfikuje przypisania lokalu")
        void shouldNotChangeApartmentWhenApartmentIdIsNull() {
            UserApartment existing = new UserApartment();
            existing.setApartment(apartment);
            existing.setUser(existingUser);
            existingUser.getUserApartments().add(existing);

            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            User result = adminUserService.updateUser(userId, updateRequest);

            assertThat(result.getUserApartments()).hasSize(1);
            assertThat(result.getUserApartments().get(0).getApartment()).isEqualTo(apartment);
        }

        @Test
        @DisplayName("Zmiana lokalu — zastępuje stare przypisanie nowym")
        void shouldReplaceApartmentWhenNewApartmentIdProvided() {
            UUID newApartmentId = UUID.randomUUID();
            Apartment newApartment = new Apartment();
            newApartment.setId(newApartmentId);
            newApartment.setNumber("10");

            UserApartment old = new UserApartment();
            old.setApartment(apartment);
            old.setUser(existingUser);
            existingUser.getUserApartments().add(old);

            updateRequest.setApartmentId(newApartmentId);

            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(apartmentRepository.findById(newApartmentId))
                    .thenReturn(Optional.of(newApartment));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            User result = adminUserService.updateUser(userId, updateRequest);

            assertThat(result.getUserApartments()).hasSize(1);
            assertThat(result.getUserApartments().get(0).getApartment()).isEqualTo(newApartment);
        }

        @Test
        @DisplayName("Nieistniejący użytkownik — rzuca IllegalArgumentException")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adminUserService.updateUser(userId, updateRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Użytkownik");
        }

        @Test
        @DisplayName("Nieistniejący lokal — rzuca IllegalArgumentException")
        void shouldThrowWhenNewApartmentNotFound() {
            UUID badApartmentId = UUID.randomUUID();
            updateRequest.setApartmentId(badApartmentId);

            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(apartmentRepository.findById(badApartmentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adminUserService.updateUser(userId, updateRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Lokal");
        }

        @Test
        @DisplayName("Nieistniejący użytkownik — nie zapisuje")
        void shouldNotSaveWhenUserNotFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adminUserService.updateUser(userId, updateRequest));

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Poprawne dane — wywołuje save na repozytorium")
        void shouldCallSaveOnRepository() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            adminUserService.updateUser(userId, updateRequest);

            verify(userRepository).save(existingUser);
        }
    }

    // -------------------------------------------------------
    // deactivateUser
    // -------------------------------------------------------

    @Nested
    @DisplayName("deactivateUser — dezaktywacja konta użytkownika")
    class DeactivateUser {

        @Test
        @DisplayName("Istniejący użytkownik — ustawia is_active = false")
        void shouldSetActiveFalse() {
            UUID userId = UUID.randomUUID();
            User user = new User();
            user.setId(userId);
            user.setEmail("jan@blokur.pl");
            user.setActive(true);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            adminUserService.deactivateUser(userId);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().isActive()).isFalse();
        }

        @Test
        @DisplayName("Istniejący użytkownik — wywołuje save na repozytorium")
        void shouldCallSaveOnRepository() {
            UUID userId = UUID.randomUUID();
            User user = new User();
            user.setEmail("jan@blokur.pl");
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            adminUserService.deactivateUser(userId);

            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Nieistniejący użytkownik — rzuca IllegalArgumentException")
        void shouldThrowWhenUserNotFound() {
            UUID userId = UUID.randomUUID();
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adminUserService.deactivateUser(userId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Użytkownik");
        }

        @Test
        @DisplayName("Nieistniejący użytkownik — nie wywołuje save")
        void shouldNotCallSaveWhenUserNotFound() {
            UUID userId = UUID.randomUUID();
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adminUserService.deactivateUser(userId));

            verify(userRepository, never()).save(any());
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
