package postgres.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA entity mapped to the {@code museum_artifact} table.
 *
 * <p>{@code artifactId} is a UUID primary key. Hibernate generates one automatically
 * (Strategy 1) when the field is {@code null} at persist time, or uses a caller-supplied
 * value (Strategy 2) when it is already set.
 */
@Entity
@Table(name = "museum_artifact")
@Builder
@Getter
@ToString
@NoArgsConstructor          // required by JPA
@AllArgsConstructor         // required by Lombok @Builder
public class MuseumArtifact {

    /** Primary key – Hibernate pre-generates a UUID (random/v4) if null before INSERT. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "artifact_id", updatable = false, nullable = false)
    private UUID artifactId;

    @Column(name = "artifact_name", nullable = false)
    private String artifactName;

    @Column(name = "description")
    private String description;

    @Column(name = "acquired_on")
    private LocalDate acquiredOn;

    /**
     * Set by Hibernate just before INSERT via @CreationTimestamp.
     * Maps to the {@code created_at TIMESTAMPTZ} column.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
