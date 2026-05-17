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
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Encja reprezentująca konkretny, faktyczny oddany głos przez danego lokatora (użytkownika) w
 * poszczególnej uchwale na konkretną jej opcję.
 */
@Entity
@Table(
        name = "resolution_votes",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"resolution_id", "voter_id"})})
@Getter
@Setter
public class ResolutionVote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolution_id", nullable = false)
    private Resolution resolution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    private ResolutionOption option;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voter_id", nullable = false)
    private User voter;

    @CreationTimestamp
    @Column(name = "voted_at")
    @ColumnDefault("CURRENT_TIMESTAMP")
    private LocalDateTime votedAt;

    /**
     * Ustawia którą z dostępnych opcji wybrał głosujący użytkownik.
     *
     * @param option Opcja oddanego głosu
     */
    public void setOption(ResolutionOption option) {
        this.option = option;
        if (option != null) {
            this.resolution = option.getResolution();
        }
    }
}
