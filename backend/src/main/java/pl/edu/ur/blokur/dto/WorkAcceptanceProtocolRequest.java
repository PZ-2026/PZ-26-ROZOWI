package pl.edu.ur.blokur.dto;

import java.util.List;
import lombok.Data;

/** DTO z danymi do wygenerowania protokołu odbioru prac. */
@Data
public class WorkAcceptanceProtocolRequest {

    private String ticketNumber;
    private String workDescription;
    private String maintenanceWorkerName;
    private List<String> beforeImagesPaths;
    private List<String> afterImagesPaths;

    public WorkAcceptanceProtocolRequest() {
        this.beforeImagesPaths = new java.util.ArrayList<>();
        this.afterImagesPaths = new java.util.ArrayList<>();
    }

    /**
     * Tworzy żądanie wygenerowania protokołu odbioru prac.
     *
     * @param ticketNumber numer zgłoszenia, którego dotyczy protokół
     * @param workDescription opis wykonanych prac
     * @param maintenanceWorkerName imię i nazwisko pracownika wykonującego prace
     */
    public WorkAcceptanceProtocolRequest(
            String ticketNumber, String workDescription, String maintenanceWorkerName) {
        this.ticketNumber = ticketNumber;
        this.workDescription = workDescription;
        this.maintenanceWorkerName = maintenanceWorkerName;
        this.beforeImagesPaths = new java.util.ArrayList<>();
        this.afterImagesPaths = new java.util.ArrayList<>();
    }

    public WorkAcceptanceProtocolRequest(
            String ticketNumber,
            String workDescription,
            String maintenanceWorkerName,
            List<String> beforeImagesPaths,
            List<String> afterImagesPaths) {
        this.ticketNumber = ticketNumber;
        this.workDescription = workDescription;
        this.maintenanceWorkerName = maintenanceWorkerName;
        this.beforeImagesPaths = beforeImagesPaths;
        this.afterImagesPaths = afterImagesPaths;
    }
}
