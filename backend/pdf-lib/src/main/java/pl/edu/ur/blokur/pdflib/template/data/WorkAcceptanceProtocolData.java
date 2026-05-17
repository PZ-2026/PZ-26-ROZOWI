package pl.edu.ur.blokur.pdflib.template.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Dane wejściowe dla protokołu odbioru prac konserwatorskich. Klasa POJO bez zależności od Springa
 * ani Lomboka — pełna kompatybilność z używaniem biblioteki w dowolnym środowisku.
 */
public final class WorkAcceptanceProtocolData {

    private final String ticketNumber;
    private final String workDescription;
    private final String maintenanceWorkerName;
    private final List<String> beforeImagesPaths;
    private final List<String> afterImagesPaths;

    /**
     * Tworzy dane protokołu z pustymi listami zdjęć przed/po.
     *
     * @param ticketNumber numer zgłoszenia
     * @param workDescription opis wykonanych prac
     * @param maintenanceWorkerName imię i nazwisko konserwatora
     */
    public WorkAcceptanceProtocolData(
            String ticketNumber, String workDescription, String maintenanceWorkerName) {
        this(
                ticketNumber,
                workDescription,
                maintenanceWorkerName,
                new ArrayList<>(),
                new ArrayList<>());
    }

    /**
     * Tworzy dane protokołu wraz ze ścieżkami do zdjęć przed i po naprawie.
     *
     * @param ticketNumber numer zgłoszenia
     * @param workDescription opis wykonanych prac
     * @param maintenanceWorkerName imię i nazwisko konserwatora
     * @param beforeImagesPaths absolutne ścieżki do zdjęć sprzed naprawy (może być pusta)
     * @param afterImagesPaths absolutne ścieżki do zdjęć po naprawie (może być pusta)
     */
    public WorkAcceptanceProtocolData(
            String ticketNumber,
            String workDescription,
            String maintenanceWorkerName,
            List<String> beforeImagesPaths,
            List<String> afterImagesPaths) {
        this.ticketNumber = ticketNumber;
        this.workDescription = workDescription;
        this.maintenanceWorkerName = maintenanceWorkerName;
        this.beforeImagesPaths =
                beforeImagesPaths != null ? new ArrayList<>(beforeImagesPaths) : new ArrayList<>();
        this.afterImagesPaths =
                afterImagesPaths != null ? new ArrayList<>(afterImagesPaths) : new ArrayList<>();
    }

    /** @return numer zgłoszenia */
    public String getTicketNumber() {
        return ticketNumber;
    }

    /** @return opis wykonanych prac */
    public String getWorkDescription() {
        return workDescription;
    }

    /** @return imię i nazwisko konserwatora */
    public String getMaintenanceWorkerName() {
        return maintenanceWorkerName;
    }

    /** @return niezmieniona lista ścieżek do zdjęć sprzed naprawy */
    public List<String> getBeforeImagesPaths() {
        return Collections.unmodifiableList(beforeImagesPaths);
    }

    /** @return niezmieniona lista ścieżek do zdjęć po naprawie */
    public List<String> getAfterImagesPaths() {
        return Collections.unmodifiableList(afterImagesPaths);
    }
}
