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

        if (userDeviceRepository.existsByFcmTokenAndUserId(fcmToken, userId)) {
            return;
        }

        // Token może być przypisany do innego użytkownika (np. po przelogowaniu) — zaaktualizuj go
        var existingDevice = userDeviceRepository.findByFcmToken(fcmToken);
        if (existingDevice.isPresent()) {
            var device = existingDevice.get();
            device.setUserId(userId);
            device.setPlatform(platform);
            userDeviceRepository.save(device);
        } else {
            var device = new UserDevice();
            device.setUserId(userId);
            device.setFcmToken(fcmToken);
            device.setPlatform(platform);
            userDeviceRepository.save(device);
        }
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
