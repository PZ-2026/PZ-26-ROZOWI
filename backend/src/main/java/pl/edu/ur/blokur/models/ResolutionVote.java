package pl.edu.ur.blokur.models;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Encja reprezentująca konkretny, faktyczny oddany głos przez danego
 * lokatora (użytkownika) w poszczególnej uchwale na konkretną jej opcję.
 */
@Entity
@Table(name = "resolution_votes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"resolution_id", "voter_id"})
})
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
     * Zwraca unikalny identyfikator głosu.
     *
     * @return Identyfikator UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Ustawia unikalny identyfikator rekordu oddanego głosu.
     *
     * @param id Identyfikator
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Zwraca powiązaną uchwałę, na którą został oddany głos.
     *
     * @return instancja Resolution
     */
    public Resolution getResolution() {
        return resolution;
    }

    /**
     * Ustawia uchwałę, będącą tematem tego oddanego głosu.
     *
     * @param resolution uchwała
     */
    public void setResolution(Resolution resolution) {
        this.resolution = resolution;
    }

    /**
     * Pobiera wybraną przez użytkownika opcję we wskazanym głosowaniu.
     *
     * @return Zaznaczona ResolutionOption
     */
    public ResolutionOption getOption() {
        return option;
    }

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

    /**
     * Zwraca uzytkownika, do którego przynależy w sposób bezpośredni ten wygenerowany rekord głosu.
     *
     * @return instancja głosującego (User)
     */
    public User getVoter() {
        return voter;
    }

    /**
     * Ustawia osobę przesyłającą wniosek (osoba, która udokumentowała ten głos).
     *
     * @param voter Użytkownik przypisywany z Principal security Context API.
     */
    public void setVoter(User voter) {
        this.voter = voter;
    }

    /**
     * Zwraca czas wpłynięcia informacji od Mieszkańca przez API o chęci przydzielenia tego głosu na zasobie relacyjnym.
     *
     * @return LocalDateTime czas dodania
     */
    public LocalDateTime getVotedAt() {
        return votedAt;
    }

    /**
     * Ustawia czas stworzenia i poprowadzenia rekordu przez rzutowania na bazie.
     *
     * @param votedAt Moment oddania głosu
     */
    public void setVotedAt(LocalDateTime votedAt) {
        this.votedAt = votedAt;
    }
}
