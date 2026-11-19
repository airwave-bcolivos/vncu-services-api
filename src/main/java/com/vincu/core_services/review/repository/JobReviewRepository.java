package com.vincu.core_services.review.repository;

import com.vincu.core_services.review.domain.JobReview;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@NullMarked
@Repository
public interface JobReviewRepository extends JpaRepository<JobReview, UUID> {

    boolean existsByJobOfferId(UUID jobOfferId);

    @EntityGraph(attributePaths = {"jobOffer", "technician"})
    Optional<JobReview> findByJobOfferId(UUID jobOfferId);

    @EntityGraph(attributePaths = {"jobOffer", "technician"})
    Page<JobReview> findByTechnicianId(UUID technicianId, Pageable pageable);
}