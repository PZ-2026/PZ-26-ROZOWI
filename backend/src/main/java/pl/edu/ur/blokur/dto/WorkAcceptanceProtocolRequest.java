package pl.edu.ur.blokur.dto;

/**
 * DTO z danymi do wygenerowania protokołu odbioru prac.
 */
public class WorkAcceptanceProtocolRequest {

    private String ticketNumber;
    private String workDescription;
    private String maintenanceWorkerName;

    public WorkAcceptanceProtocolRequest() {
    }

    public WorkAcceptanceProtocolRequest(String ticketNumber, String workDescription, String maintenanceWorkerName) {
        this.ticketNumber = ticketNumber;
        this.workDescription = workDescription;
        this.maintenanceWorkerName = maintenanceWorkerName;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getWorkDescription() {
        return workDescription;
    }

    public void setWorkDescription(String workDescription) {
        this.workDescription = workDescription;
    }

    public String getMaintenanceWorkerName() {
        return maintenanceWorkerName;
    }

    public void setMaintenanceWorkerName(String maintenanceWorkerName) {
        this.maintenanceWorkerName = maintenanceWorkerName;
    }
}
