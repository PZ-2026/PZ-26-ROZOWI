package pl.edu.ur.blokur.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Konfiguracja Firebase Admin SDK. Inicjalizuje aplikację Firebase na podstawie pliku klucza
 * serwisowego wskazanego przez zmienną środowiskową FCM_CREDENTIALS_PATH.
 */
@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${fcm.credentials.path:#{null}}")
    private String credentialsPath;

    /**
     * Inicjalizuje Firebase Admin SDK. Jeśli FCM_CREDENTIALS_PATH nie jest skonfigurowana lub plik
     * nie istnieje, aplikacja startuje bez FCM (powiadomienia PUSH są pomijane).
     */
    @PostConstruct
    public void initialize() {
        if (credentialsPath == null || credentialsPath.isBlank()) {
            log.warn(
                    "FCM_CREDENTIALS_PATH nie jest skonfigurowana — powiadomienia PUSH są"
                            + " wyłączone");
            return;
        }

        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        try (FileInputStream serviceAccount = new FileInputStream(credentialsPath)) {
            FirebaseOptions options =
                    FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .build();
            FirebaseApp.initializeApp(options);
            log.info("Firebase Admin SDK zainicjalizowany pomyślnie");
        } catch (IOException e) {
            log.error(
                    "Błąd inicjalizacji Firebase Admin SDK (credentials: {}): {}",
                    credentialsPath,
                    e.getMessage());
        }
    }
}
