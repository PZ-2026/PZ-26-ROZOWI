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

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * Konfiguracja Firebase Admin SDK. Inicjalizuje aplikację Firebase na podstawie pliku klucza
 * serwisowego (FCM_CREDENTIALS_PATH) lub zawartości JSON bezpośrednio z pliku .env (FCM_CREDENTIALS_JSON).
 */
@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${fcm.credentials.path:#{null}}")
    private String credentialsPath;

    @Value("${fcm.credentials.json:#{null}}")
    private String credentialsJson;

    /**
     * Inicjalizuje Firebase Admin SDK. Jeśli FCM_CREDENTIALS_JSON i FCM_CREDENTIALS_PATH 
     * nie są skonfigurowane, aplikacja startuje bez FCM (powiadomienia PUSH są pomijane).
     */
    @PostConstruct
    public void initialize() {
        if ((credentialsPath == null || credentialsPath.isBlank()) &&
            (credentialsJson == null || credentialsJson.isBlank())) {
            log.warn("FCM_CREDENTIALS_PATH ani FCM_CREDENTIALS_JSON nie są skonfigurowane — powiadomienia PUSH są wyłączone");
            return;
        }

        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        try {
            GoogleCredentials credentials;
            
            if (credentialsJson != null && !credentialsJson.isBlank()) {
                // Wczytanie prosto ze zmiennej środowiskowej (np. z .env)
                ByteArrayInputStream stream = new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8));
                credentials = GoogleCredentials.fromStream(stream);
                log.info("Wczytywanie konfiguracji Firebase z zawartości JSON (FCM_CREDENTIALS_JSON)");
            } else {
                // Wczytanie z pliku fizycznego
                try (FileInputStream serviceAccount = new FileInputStream(credentialsPath)) {
                    credentials = GoogleCredentials.fromStream(serviceAccount);
                    log.info("Wczytywanie konfiguracji Firebase z pliku: {}", credentialsPath);
                }
            }

            FirebaseOptions options =
                    FirebaseOptions.builder()
                            .setCredentials(credentials)
                            .build();
            FirebaseApp.initializeApp(options);
            log.info("Firebase Admin SDK zainicjalizowany pomyślnie");
        } catch (IOException e) {
            log.error(
                    "Błąd inicjalizacji Firebase Admin SDK: {}",
                    e.getMessage());
        }
    }
}
