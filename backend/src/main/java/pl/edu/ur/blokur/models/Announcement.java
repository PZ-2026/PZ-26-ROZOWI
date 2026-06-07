package pl.edu.ur.blokur.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Encja reprezentująca ogłoszenie lub komunikat przypisany do konkretnego zasięgu (globalny, dla
 * budynku, klatki lub mieszkania).
 */
@Entity
@Table(name = "announcements")
@Getter
@Setter
@NoArgsConstructor
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "type", length = 50)
    @ColumnDefault("'OGLOSZENIE'")
    private String type = "OGLOSZENIE";

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_building_id")
    private Building targetBuilding;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_staircase_id")
    private Staircase targetStaircase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_apartment_id")
    private Apartment targetApartment;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private AnnouncementTargetType targetType = AnnouncementTargetType.WSZYSCY;

    @Column(name = "attachment_path", length = 500)
    private String attachmentPath;

    @Column(name = "planned_date")
    private LocalDateTime plannedDate;

    @CreationTimestamp
    @Column(name = "created_at")
    @ColumnDefault("CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
}
