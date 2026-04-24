package pl.edu.ur.blokur.dto;

/** DTO z danymi do wygenerowania protokołu odbioru prac. */
public class WorkAcceptanceProtocolRequest {

    private String ticketNumber;
    private String workDescription;
    private String maintenanceWorkerName;

    /** Konstruktor bezargumentowy wymagany przez deserializację Jacksona. */
    public WorkAcceptanceProtocolRequest() {}

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
    }

    /**
     * Zwraca numer zgłoszenia, którego dotyczy protokół.
     *
     * @return numer zgłoszenia
     */
    public String getTicketNumber() {
        return ticketNumber;
    }

    /**
     * Ustawia numer zgłoszenia, którego dotyczy protokół.
     *
     * @param ticketNumber numer zgłoszenia
     */
    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    /**
     * Zwraca opis wykonanych prac.
     *
     * @return opis prac
     */
    public String getWorkDescription() {
        return workDescription;
    }

    /**
     * Ustawia opis wykonanych prac.
     *
     * @param workDescription opis prac
     */
    public void setWorkDescription(String workDescription) {
        this.workDescription = workDescription;
    }

    /**
     * Zwraca imię i nazwisko pracownika wykonującego prace.
     *
     * @return imię i nazwisko pracownika
     */
    public String getMaintenanceWorkerName() {
        return maintenanceWorkerName;
    }

    /**
     * Ustawia imię i nazwisko pracownika wykonującego prace.
     *
     * @param maintenanceWorkerName imię i nazwisko pracownika
     */
    public void setMaintenanceWorkerName(String maintenanceWorkerName) {
        this.maintenanceWorkerName = maintenanceWorkerName;
    }
}
