package pl.edu.ur.blokur.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

/** DTO z informacją o saldzie pojedynczego lokalu zwracane w zestawieniu zaległości. */
@Data
public class ApartmentBalanceResponse {

    private final UUID apartmentId;
    private final String address;
    private final BigDecimal balance;
    private final LocalDate lastPaymentDate;
    private final Long daysOverdue;
}
