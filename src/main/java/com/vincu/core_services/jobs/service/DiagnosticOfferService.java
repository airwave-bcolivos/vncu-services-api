package com.vincu.core_services.jobs.service;

import com.vincu.core_services.jobs.domain.JobOffer;
import com.vincu.core_services.jobs.domain.JobStatus;
import com.vincu.core_services.jobs.domain.DiagnosticOffer;
import com.vincu.core_services.jobs.dto.DiagnosticOfferRequest;
import com.vincu.core_services.jobs.dto.DiagnosticOfferResponse;
import com.vincu.core_services.jobs.dto.PageResponse;
import com.vincu.core_services.jobs.dto.PendingDiagnosticJobResponse;
import com.vincu.core_services.jobs.repository.DiagnosticOfferRepository;
import com.vincu.core_services.jobs.repository.JobOfferRepository;
import com.vincu.core_services.users.repository.TechnicianProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static com.vincu.core_services.jobs.domain.DiagnosticOfferStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiagnosticOfferService {
    private final JobOfferRepository jobs;
    private final DiagnosticOfferRepository offers;
    private final TechnicianProfileRepository technicians;

    public PageResponse<PendingDiagnosticJobResponse> listPendingJobs(UUID technicianId, int page, int size) {
        var pageable = PageRequest.of(Math.max(0, page), Math.min(50, Math.max(1, size)));
        return PageResponse.from(offers.findSubmittedJobs(technicianId, PENDING, JobStatus.OPEN, pageable));
    }

    @Transactional
    public DiagnosticOfferResponse create(UUID jobId, UUID technicianId, DiagnosticOfferRequest request) {
        var technician = technicians.findById(technicianId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        JobOffer job = jobs.findByIdForUpdate(jobId).orElseThrow(DiagnosticOfferService::jobNotFound);
        requireOpen(job);
        if (offers.existsByJobOffer_IdAndTechnician_UserId(jobId, technicianId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya enviaste una oferta para esta publicación");
        }
        var offer = new DiagnosticOffer(job, technician, request.diagnosticPrice(), request.description().strip());
        return DiagnosticOfferResponse.from(offers.save(offer));
    }

    public List<DiagnosticOfferResponse> list(UUID jobId, UUID clientId) {
        JobOffer job = jobs.findById(jobId).orElseThrow(DiagnosticOfferService::jobNotFound);
        requireOwner(job, clientId);
        return offers.findByJobOffer_IdOrderByCreatedAtDescIdAsc(jobId).stream()
                .map(DiagnosticOfferResponse::from).toList();
    }

    @Transactional
    public DiagnosticOfferResponse accept(UUID jobId, UUID offerId, UUID clientId) {
        JobOffer job = jobs.findByIdForUpdate(jobId).orElseThrow(DiagnosticOfferService::jobNotFound);
        requireOwner(job, clientId);
        var offer = offers.findByIdAndJobOffer_Id(offerId, jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La oferta de diagnóstico no existe"));
        if (offer.getStatus() == ACCEPTED && job.getStatus() == JobStatus.IN_PROGRESS
                && offer.getTechnician().getUserId().equals(job.getTechnicianId())) {
            return DiagnosticOfferResponse.from(offer);
        }
        requireOpen(job);
        if (offer.getStatus() != PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La oferta de diagnóstico ya no está pendiente");
        }
        offer.accept();
        job.setTechnicianId(offer.getTechnician().getUserId());
        job.setStatus(JobStatus.IN_PROGRESS);
        offers.rejectPending(jobId, PENDING, REJECTED);
        return DiagnosticOfferResponse.from(offer);
    }

    private static void requireOpen(JobOffer job) {
        if (job.getStatus() != JobStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La publicación ya no está abierta");
        }
    }

    private static void requireOwner(JobOffer job, UUID clientId) {
        if (!job.getClientId().equals(clientId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private static ResponseStatusException jobNotFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "La publicación no existe");
    }
}
