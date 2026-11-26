package com.vincu.core_services.jobs.repository;

import com.vincu.core_services.jobs.domain.DiagnosticOffer;
import com.vincu.core_services.jobs.domain.DiagnosticOfferStatus;
import com.vincu.core_services.jobs.domain.JobStatus;
import com.vincu.core_services.jobs.dto.PendingDiagnosticJobResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface DiagnosticOfferRepository extends JpaRepository<DiagnosticOffer, UUID> {
    boolean existsByJobOffer_IdAndTechnician_UserId(UUID jobOfferId, UUID technicianId);

    @Query("""
            select new com.vincu.core_services.jobs.dto.PendingDiagnosticJobResponse(
                job.id, job.title, job.specialty, job.description, job.createdAt)
            from DiagnosticOffer offer join offer.jobOffer job
            where offer.technician.userId = :technicianId
              and offer.status = :offerStatus and job.status = :jobStatus
            order by offer.createdAt desc, offer.id asc
            """)
    Page<PendingDiagnosticJobResponse> findSubmittedJobs(
            UUID technicianId, DiagnosticOfferStatus offerStatus, JobStatus jobStatus, Pageable pageable);

    @EntityGraph(attributePaths = "technician")
    List<DiagnosticOffer> findByJobOffer_IdOrderByCreatedAtDescIdAsc(UUID jobOfferId);

    @EntityGraph(attributePaths = "technician")
    Optional<DiagnosticOffer> findByIdAndJobOffer_Id(UUID id, UUID jobOfferId);

    @Query("""
            select offer.jobOffer.id as jobId, count(offer) as total
            from DiagnosticOffer offer
            where offer.jobOffer.id in :jobIds
            group by offer.jobOffer.id
            """)
    List<JobOfferCount> countByJobIds(Collection<UUID> jobIds);

    interface JobOfferCount {
        UUID getJobId();
        long getTotal();
    }

    @Modifying(flushAutomatically = true)
    @Query("""
            update DiagnosticOffer offer
            set offer.status = :rejected, offer.updatedAt = CURRENT_TIMESTAMP
            where offer.jobOffer.id = :jobId and offer.status = :pending
            """)
    void rejectPending(UUID jobId, DiagnosticOfferStatus pending, DiagnosticOfferStatus rejected);
}
