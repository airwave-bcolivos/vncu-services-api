package com.vincu.core_services.users.repository;

import com.vincu.core_services.users.domain.TechnicianProfile;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TechnicianProfileRepository extends JpaRepository<TechnicianProfile, UUID> {

    // Bloquea la fila del técnico hasta terminar la transacción
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TechnicianProfile t WHERE t.userId = :id")
    Optional<TechnicianProfile> findByIdForUpdate(@Param("id") UUID id);

    // Recalcula promedio y cantidad desde job_reviews
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            UPDATE technician_profiles
            SET average_rating = (SELECT ROUND(AVG(rating), 2) FROM job_reviews WHERE technician_id = :id),
                rating_count   = (SELECT COUNT(*) FROM job_reviews WHERE technician_id = :id)
            WHERE user_id = :id
            """, nativeQuery = true)
    int refreshRatingSummary(@Param("id") UUID id);
}
