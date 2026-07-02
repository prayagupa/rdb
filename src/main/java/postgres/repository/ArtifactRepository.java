package postgres.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import postgres.entity.MuseumArtifact;

import java.util.UUID;

/**
 * Spring Data JPA repository for {@link MuseumArtifact}.
 *
 * Spring auto-generates a proxy implementation at runtime; no additional code needed for
 * standard CRUD operations (save, findById, findAll, delete, …).
 */
@Repository
public interface ArtifactRepository extends JpaRepository<MuseumArtifact, UUID> {
}

