package com.vincu.core_services.users.repository;

import com.vincu.core_services.users.domain.ClientProfile;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ClientProfileRepository extends JpaRepository<ClientProfile, UUID> {

    @EntityGraph(attributePaths = {"user", "documentType"})
    Optional<ClientProfile> findWithDetailsByUserId(UUID userid);

    boolean existsByDocumentId(String documentId);

    // Bloquea la fila del cliente hasta terminar la transacción
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ClientProfile c WHERE c.userId = :id")
    Optional<ClientProfile> findByIdForUpdate(@Param("id") UUID id);


    // Recalcula promedio y cantidad desde client_reviews
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            UPDATE client_profiles
            SET average_rating = (SELECT ROUND(AVG(rating), 2) FROM client_reviews WHERE client_id = :id),
                rating_count   = (SELECT COUNT(*) FROM client_reviews WHERE client_id = :id)
            WHERE user_id = :id
            """, nativeQuery = true)
    int refreshRatingSummary(@Param("id") UUID id);
}
