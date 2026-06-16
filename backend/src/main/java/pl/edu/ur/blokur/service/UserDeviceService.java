package pl.edu.ur.blokur.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.ur.blokur.exception.NotFoundException;
import pl.edu.ur.blokur.models.UserDevice;
import pl.edu.ur.blokur.repository.UserDeviceRepository;
import pl.edu.ur.blokur.repository.UserRepository;

/** Serwis zarządzający urządzeniami użytkowników i tokenami FCM. */
@Service
public class UserDeviceService {

    private final UserDeviceRepository userDeviceRepository;
    private final UserRepository userRepository;

    public UserDeviceService(
            UserDeviceRepository userDeviceRepository, UserRepository userRepository) {
        this.userDeviceRepository = userDeviceRepository;
        this.userRepository = userRepository;
    }

    /**
     * Rejestruje token FCM urządzenia dla zalogowanego użytkownika. Jeśli token już istnieje w
     * bazie dla tego użytkownika, operacja jest idempotentna.
     *
     * @param userId identyfikator użytkownika
     * @param fcmToken token FCM urządzenia
     * @param platform platforma urządzenia (np. ANDROID, IOS)
     */
    @Transactional
    public void registerDevice(UUID userId, String fcmToken, String platform) {
        userRepository
                .findById(userId)
                .orElseThrow(() -> new NotFoundException("Użytkownik nie istnieje"));

        // Zlecamy całkowitą kontrolę nad unikalnością bazie danych.
        // Nawet jeśli 10 urządzeń wyśle to w tej samej mikrosekundzie, baza przetworzy to atomowo (UPSERT).
        userDeviceRepository.upsertFcmToken(fcmToken, userId, platform);
    }

    /**
     * Usuwa token FCM urządzenia użytkownika (wyrejestrowanie z powiadomień PUSH).
     *
     * @param userId identyfikator użytkownika
     * @param fcmToken token FCM do usunięcia
     */
    @Transactional
    public void unregisterDevice(UUID userId, String fcmToken) {
        userDeviceRepository.deleteByFcmTokenAndUserId(fcmToken, userId);
    }
}
