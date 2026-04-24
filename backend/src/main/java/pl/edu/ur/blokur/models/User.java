package pl.edu.ur.blokur.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/** Encja reprezentująca użytkownika systemu Blokur. */
@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "role", nullable = false, length = 50)
    private String role;

    @ColumnDefault("true")
    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ColumnDefault("false")
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserApartment> userApartments = new ArrayList<>();

    /** Konstruktor bezargumentowy wymagany przez JPA. */
    public User() {}

    /**
     * Zwraca unikalny identyfikator użytkownika.
     *
     * @return identyfikator UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Ustawia unikalny identyfikator użytkownika.
     *
     * @param id identyfikator UUID
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Zwraca adres e-mail użytkownika (używany jako login).
     *
     * @return adres e-mail
     */
    public String getEmail() {
        return email;
    }

    /**
     * Ustawia adres e-mail użytkownika.
     *
     * @param email adres e-mail
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Zwraca zahashowane hasło użytkownika.
     *
     * @return hash hasła (BCrypt)
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Ustawia zahashowane hasło użytkownika.
     *
     * @param passwordHash hash hasła (BCrypt)
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Zwraca imię użytkownika.
     *
     * @return imię
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Ustawia imię użytkownika.
     *
     * @param firstName imię
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Zwraca nazwisko użytkownika.
     *
     * @return nazwisko
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Ustawia nazwisko użytkownika.
     *
     * @param lastName nazwisko
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Zwraca rolę użytkownika w systemie (np. MIESZKANIEC, ZARZADCA).
     *
     * @return rola użytkownika
     */
    public String getRole() {
        return role;
    }

    /**
     * Ustawia rolę użytkownika w systemie.
     *
     * @param role rola użytkownika
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Informuje, czy konto użytkownika jest aktywne.
     *
     * @return {@code true} jeśli konto jest aktywne
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Ustawia stan aktywności konta użytkownika.
     *
     * @param active {@code false} aby zablokować konto
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Zwraca datę i czas utworzenia konta.
     *
     * @return data i czas rejestracji
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Ustawia datę i czas utworzenia konta.
     *
     * @param createdAt data i czas rejestracji
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Informuje, czy konto zostało oznaczone jako usunięte (soft delete).
     *
     * @return {@code true} jeśli konto jest usunięte
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * Ustawia flagę usunięcia konta (soft delete — rekord pozostaje w bazie).
     *
     * @param deleted {@code true} aby oznaczyć konto jako usunięte
     */
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * Zwraca listę powiązań użytkownika z lokalami mieszkalnymi.
     *
     * @return lista powiązań UserApartment
     */
    public List<UserApartment> getUserApartments() {
        return userApartments;
    }

    /**
     * Ustawia listę powiązań użytkownika z lokalami mieszkalnymi.
     *
     * @param userApartments lista powiązań UserApartment
     */
    public void setUserApartments(List<UserApartment> userApartments) {
        this.userApartments = userApartments;
    }
}
