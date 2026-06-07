package pl.edu.ur.blokur.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

/** Encja reprezentująca konkretną opcję wyboru (np. "Za", "Przeciw") w ramach danej uchwały. */
@Entity
@Table(
        name = "resolution_options",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"id", "resolution_id"})})
@Getter
@Setter
public class ResolutionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolution_id", nullable = false)
    private Resolution resolution;

    @Column(name = "option_text", nullable = false, length = 255)
    private String optionText;
}
