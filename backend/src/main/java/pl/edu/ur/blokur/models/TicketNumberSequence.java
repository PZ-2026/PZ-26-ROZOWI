package pl.edu.ur.blokur.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Encja przechowująca kolejną wartość sekwencji numerów zgłoszeń per rok. Wiersz blokowany
 * pesymistycznie ({@code SELECT FOR UPDATE}) gwarantuje unikalność w środowiskach
 * wieloinstancyjnych.
 */
@Entity
@Table(name = "ticket_number_sequences")
@Getter
@Setter
@NoArgsConstructor
public class TicketNumberSequence {

    /** Rok kalendarzowy, dla którego przechowywana jest sekwencja. */
    @Id
    @Column(name = "year", nullable = false)
    private Integer year;

    /** Następna wolna wartość sekwencji (zwracana, a następnie inkrementowana). */
    @Column(name = "next_val", nullable = false)
    private int nextVal;
}
