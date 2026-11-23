package com.vincu.core_services.review.repository;

import com.vincu.core_services.review.domain.ClientReview;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

public interface ClientReviewRepository extends JpaRepository<ClientReview, UUID> {

    boolean existsByJobOfferId(UUID jobOfferId);

    @EntityGraph(attributePaths = {"jobOffer", "client"})
    Optional<ClientReview> findByJobOfferId(UUID jobOfferId);

    @EntityGraph(attributePaths = {"jobOffer", "client"})
    Page<ClientReview> findByClientId(UUID clientId, Pageable pageable);

}
