package pl.edu.ur.blokur.service;

import java.time.LocalDate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

/**
 * Serwis generujący unikalne numery zgłoszeń w formacie {@code ZGL/RRRR/NNN}.
 *
 * <p>Numery są generowane sekwencyjnie per rok kalendarzowy. Generator jest thread-safe — używa
 * {@link AtomicInteger} per rok, co eliminuje wyścigi przy równoległych żądaniach.
 *
 * <p>Przykład wygenerowanego numeru: {@code ZGL/2026/001}.
 */
@Service
public class TicketNumberGenerator {

    private final ConcurrentHashMap<Integer, AtomicInteger> countersByYear =
            new ConcurrentHashMap<>();

    /**
     * Generuje kolejny unikalny numer zgłoszenia dla bieżącego roku.
     *
     * <p>Sekwencja zaczyna się od 1 dla każdego nowego roku i jest zerowana automatycznie. Numer ma
     * format {@code ZGL/RRRR/NNN}, gdzie NNN jest wypełniony zerami do 3 cyfr.
     *
     * @return numer zgłoszenia, np. {@code ZGL/2026/001}
     */
    public String generate() {
        int year = LocalDate.now().getYear();
        var counter = countersByYear.computeIfAbsent(year, y -> new AtomicInteger(0));
        int next = counter.incrementAndGet();
        return String.format("ZGL/%d/%03d", year, next);
    }

    /**
     * Inicjalizuje lub nadpisuje licznik dla podanego roku. Używane przy uruchomieniu aplikacji w
     * celu synchronizacji z ostatnim numerem zapisanym w bazie danych.
     *
     * @param year rok kalendarzowy
     * @param lastValue ostatnia użyta wartość sekwencji (następny numer = lastValue + 1)
     */
    public void initYear(int year, int lastValue) {
        countersByYear.put(year, new AtomicInteger(lastValue));
    }
}
