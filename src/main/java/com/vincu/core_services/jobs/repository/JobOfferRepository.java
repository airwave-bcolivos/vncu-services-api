package com.vincu.core_services.jobs.repository;

import com.vincu.core_services.jobs.domain.JobOffer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

@NullMarked
@Repository
public interface JobOfferRepository extends JpaRepository<JobOffer, UUID>, JpaSpecificationExecutor<JobOffer> {

    @EntityGraph(attributePaths = "district")
    Page<JobOffer> findByClientId(UUID clientId, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "district")
    Page<JobOffer> findAll(@Nullable Specification<JobOffer> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"district", "client", "technician"})
    Optional<JobOffer> findWithDetailsById(UUID id);

}
