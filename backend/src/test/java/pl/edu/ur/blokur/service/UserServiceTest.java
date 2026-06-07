package pl.edu.ur.blokur.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.ur.blokur.dto.UserWithTicketsDto;
import pl.edu.ur.blokur.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService — serwis uzytkownikow")
class UserServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private UserService userService;

    @Test
    @DisplayName("Powinno pobrac uzytkownikow z aktywnymi zgloszeniami")
    void shouldGetUsersWithActiveTickets() {
        String role = "KONSERWATOR";
        UserWithTicketsDto dto =
                new UserWithTicketsDto(
                        UUID.randomUUID(), "Jan", "Kowalski", "jan@blokur.pl", "123456789", 5);
        when(userRepository.findUsersWithActiveTicketsByRole(role)).thenReturn(List.of(dto));

        List<UserWithTicketsDto> result = userService.getUsersWithActiveTicketCountByRole(role);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getActiveTicketsCount()).isEqualTo(5);
        verify(userRepository).findUsersWithActiveTicketsByRole(role);
    }

    @Test
    @DisplayName("getMe — powinno zwrocic dane zalogowanego uzytkownika")
    void shouldReturnMe() {
        String email = "jan@blokur.pl";
        pl.edu.ur.blokur.models.User user = new pl.edu.ur.blokur.models.User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setFirstName("Jan");
        user.setLastName("Kowalski");
        user.setRole("MIESZKANIEC");
        user.setPhone("123456789");

        pl.edu.ur.blokur.models.Apartment apartment = new pl.edu.ur.blokur.models.Apartment();
        apartment.setId(UUID.randomUUID());

        pl.edu.ur.blokur.models.UserApartment ua = new pl.edu.ur.blokur.models.UserApartment();
        ua.setUser(user);
        ua.setApartment(apartment);
        user.getUserApartments().add(ua);

        when(userRepository.findByEmail(email)).thenReturn(java.util.Optional.of(user));

        var result = userService.getMe(email);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getFirstName()).isEqualTo("Jan");
        assertThat(result.getLastName()).isEqualTo("Kowalski");
        assertThat(result.getRole()).isEqualTo("MIESZKANIEC");
        assertThat(result.getApartmentId()).isEqualTo(apartment.getId());
    }
}
