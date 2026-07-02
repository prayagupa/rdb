package postgres;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import postgres.config.PersistenceConfig;
import postgres.entity.MuseumArtifact;
import postgres.repository.ArtifactRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring service that demonstrates UUID primary-key usage with JPA / Hibernate 6.
 *
 * <h2>UUID strategies</h2>
 * <ol>
 *   <li><b>Hibernate-generated UUID</b> – leave {@code artifactId} null when building the
 *       entity; Hibernate calls its internal UUID generator (v4/random) before the INSERT and
 *       populates the field automatically. This is wired via
 *       {@code @GeneratedValue(strategy = GenerationType.UUID)} on the entity.</li>
 *   <li><b>Application-supplied UUID</b> – set {@code artifactId = UUID.randomUUID()} (or any
 *       deterministic UUID) before passing the entity to {@code repository.save()}. Hibernate
 *       detects that the identifier is already non-null and skips its own generator.</li>
 * </ol>
 *
 * <p>Prerequisite DDL: {@code db/pg/003-uuid-artifact.sql}
 */
@Service
@Transactional
public class HibernateUuidWriter {

    private final ArtifactRepository repository;

    public HibernateUuidWriter(ArtifactRepository repository) {
        this.repository = repository;
    }

    // -------------------------------------------------------------------------
    // Strategy 1: Hibernate generates the UUID
    // -------------------------------------------------------------------------

    /**
     * Saves an artifact without an explicit ID.
     * Hibernate generates a random UUID and assigns it to {@code artifactId} before the INSERT.
     *
     * @return the persisted entity – {@code getArtifactId()} is populated after save
     */
    public MuseumArtifact saveWithHibernateGeneratedUuid(
            String name, String description, LocalDate acquiredOn) {

        MuseumArtifact artifact = MuseumArtifact.builder()
                // artifactId intentionally omitted → Hibernate generates it
                .artifactName(name)
                .description(description)
                .acquiredOn(acquiredOn)
                .build();

        return repository.save(artifact);
    }

    // -------------------------------------------------------------------------
    // Strategy 2: Application supplies the UUID
    // -------------------------------------------------------------------------

    /**
     * Saves an artifact with a UUID created by the application via {@link UUID#randomUUID()}.
     * Useful when the ID must be known <em>before</em> the database round-trip
     * (e.g. for idempotent APIs or event sourcing).
     *
     * @return the same entity with the caller-supplied UUID confirmed
     */
    public MuseumArtifact saveWithApplicationProvidedUuid(
            String name, String description, LocalDate acquiredOn) {

        UUID preassignedId = UUID.randomUUID();   // generated in Java, before any DB call

        MuseumArtifact artifact = MuseumArtifact.builder()
                .artifactId(preassignedId)        // Hibernate uses this instead of generating
                .artifactName(name)
                .description(description)
                .acquiredOn(acquiredOn)
                .build();

        return repository.save(artifact);
    }

    // -------------------------------------------------------------------------
    // Read back by UUID
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Optional<MuseumArtifact> findById(UUID id) {
        return repository.findById(id);
    }

    // -------------------------------------------------------------------------
    // Demo entry point
    // -------------------------------------------------------------------------

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext ctx =
                     new AnnotationConfigApplicationContext(PersistenceConfig.class)) {

            HibernateUuidWriter writer = ctx.getBean(HibernateUuidWriter.class);

            // --- Strategy 1 ---
            System.out.println("=== Strategy 1: Hibernate-generated UUID ===");
            MuseumArtifact a1 = writer.saveWithHibernateGeneratedUuid(
                    "Rosetta Stone replica",
                    "Full-size replica of the Rosetta Stone",
                    LocalDate.of(2020, 6, 15));
            System.out.println("Saved  : " + a1);
            writer.findById(a1.getArtifactId())
                  .ifPresent(a -> System.out.println("Found  : " + a));

            // --- Strategy 2 ---
            System.out.println("\n=== Strategy 2: Application-supplied UUID ===");
            MuseumArtifact a2 = writer.saveWithApplicationProvidedUuid(
                    "Venus de Milo cast",
                    "19th-century plaster cast",
                    LocalDate.of(2018, 3, 22));
            System.out.println("Saved  : " + a2);
            writer.findById(a2.getArtifactId())
                  .ifPresent(a -> System.out.println("Found  : " + a));
        }
    }
}
