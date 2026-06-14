package pl.edu.ur.blokur.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.edu.ur.blokur.models.UserDevice;

/** Repozytorium JPA dla encji {@link UserDevice}. */
public interface UserDeviceRepository extends JpaRepository<UserDevice, UUID> {

    /**
     * Wyszukuje urządzenie po tokenie FCM.
     *
     * @param fcmToken token FCM urządzenia
     * @return opcjonalne urządzenie
     */
    Optional<UserDevice> findByFcmToken(String fcmToken);

    /**
     * Sprawdza, czy token FCM jest już zarejestrowany dla podanego użytkownika.
     *
     * @param fcmToken token FCM urządzenia
     * @param userId identyfikator użytkownika
     * @return {@code true} jeśli kombinacja token+użytkownik istnieje w bazie
     */
    boolean existsByFcmTokenAndUserId(String fcmToken, UUID userId);

    /**
     * Usuwa urządzenie o podanym tokenie FCM przypisane do konkretnego użytkownika.
     *
     * @param token token FCM do usunięcia
     * @param userId identyfikator użytkownika
     */
    @Modifying
    @Query("DELETE FROM UserDevice ud WHERE ud.fcmToken = :token AND ud.userId = :userId")
    void deleteByFcmTokenAndUserId(@Param("token") String token, @Param("userId") UUID userId);

    /**
     * Usuwa urządzenie o podanym tokenie FCM (np. gdy token wygasł).
     *
     * @param token token FCM do usunięcia
     */
    @Modifying
    @Query("DELETE FROM UserDevice ud WHERE ud.fcmToken = :token")
    void deleteByFcmToken(@Param("token") String token);

    /**
     * Zwraca listę tokenów FCM zarejestrowanych dla danego użytkownika.
     *
     * @param userId identyfikator użytkownika
     * @return lista tokenów FCM
     */
    @Query("SELECT ud.fcmToken FROM UserDevice ud WHERE ud.userId = :userId")
    List<String> findFcmTokensByUserId(@Param("userId") UUID userId);

    /**
     * Zwraca listę tokenów FCM dla zbioru użytkowników.
     *
     * @param userIds lista identyfikatorów użytkowników
     * @return lista tokenów FCM
     */
    @Query("SELECT ud.fcmToken FROM UserDevice ud WHERE ud.userId IN :userIds")
    List<String> findFcmTokensByUserIdIn(@Param("userIds") List<UUID> userIds);

    /**
     * Wstawia nowy token FCM lub aktualizuje istniejący (UPSERT na poziomie bazy danych).
     * CAŁKOWICIE zapobiega to Race Conditions (Wyścigom) w przypadku współbieżnych requestów z tego samego urządzenia.
     */
    @Modifying
    @Query(value = "INSERT INTO user_devices (id, created_at, fcm_token, platform, user_id) " +
                   "VALUES (gen_random_uuid(), CURRENT_TIMESTAMP, :token, :platform, :userId) " +
                   "ON CONFLICT (fcm_token) DO UPDATE SET user_id = EXCLUDED.user_id, platform = EXCLUDED.platform", 
           nativeQuery = true)
    void upsertFcmToken(@Param("token") String token, @Param("userId") UUID userId, @Param("platform") String platform);
}
