package pl.edu.ur.blokur.dto;

import java.util.UUID;
import lombok.Data;

@Data
public class UserWithTicketsDto {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private long activeTicketsCount;

    public UserWithTicketsDto(
            UUID id,
            String firstName,
            String lastName,
            String email,
            String phone,
            long activeTicketsCount) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.activeTicketsCount = activeTicketsCount;
    }
}
