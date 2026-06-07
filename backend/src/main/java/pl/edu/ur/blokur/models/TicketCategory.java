package pl.edu.ur.blokur.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

/** Encja reprezentująca kategorię zgłoszenia (np. awaria, usterka, inne). */
@Entity
@Table(name = "ticket_categories")
@Setter
@Getter
public class TicketCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", unique = true, nullable = false, length = 255)
    private String name;

    @Column(name = "is_active", nullable = false)
    @ColumnDefault("true")
    private boolean isActive = true;

    @Column(name = "sla_hours")
    private Integer slaHours;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ticket> tickets = new ArrayList<>();
}
