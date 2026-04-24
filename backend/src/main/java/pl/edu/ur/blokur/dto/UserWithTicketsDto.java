package pl.edu.ur.blokur.dto;

import java.util.UUID;

public class UserWithTicketsDto {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private long activeTicketsCount;

    public UserWithTicketsDto() {}

    public UserWithTicketsDto(UUID id, String firstName, String lastName, String email, String phone, long activeTicketsCount) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.activeTicketsCount = activeTicketsCount;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public long getActiveTicketsCount() {
        return activeTicketsCount;
    }

    public void setActiveTicketsCount(long activeTicketsCount) {
        this.activeTicketsCount = activeTicketsCount;
    }
}
