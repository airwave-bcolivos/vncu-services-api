package com.vincu.core_services.review.service;

import com.vincu.core_services.jobs.domain.JobOffer;
import com.vincu.core_services.jobs.domain.JobStatus;
import com.vincu.core_services.jobs.dto.PageResponse;
import com.vincu.core_services.jobs.repository.JobOfferRepository;
import com.vincu.core_services.review.domain.JobReview;
import com.vincu.core_services.review.dto.JobReviewRequest;
import com.vincu.core_services.review.dto.JobReviewResponse;
import com.vincu.core_services.review.repository.JobReviewRepository;
import com.vincu.core_services.users.domain.TechnicianProfile;
import com.vincu.core_services.users.repository.TechnicianProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class JobReviewService {

    private static final int MAX_PAGE_SIZE = 50;

    private final JobReviewRepository reviewRepository;
    private final JobOfferRepository jobOfferRepository;
    private final TechnicianProfileRepository technicianProfileRepository;

    public JobReviewService(JobReviewRepository reviewRepository,
                            JobOfferRepository jobOfferRepository,
                            TechnicianProfileRepository technicianProfileRepository) {
        this.reviewRepository = reviewRepository;
        this.jobOfferRepository = jobOfferRepository;
        this.technicianProfileRepository = technicianProfileRepository;
    }


    @Transactional
    public JobReviewResponse createReview(UUID jobOfferId, JobReviewRequest request) {

        JobOffer offer = jobOfferRepository.findById(jobOfferId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "La oferta de trabajo no existe"));

        if (!offer.getClientId().equals(request.clientId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Solo el cliente que publicó el trabajo puede calificarlo");
        }

        if (offer.getStatus() != JobStatus.COMPLETED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Solo se pueden calificar trabajos finalizados");
        }

        UUID technicianId = offer.getTechnicianId();
        if (technicianId == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "El trabajo no tiene un técnico asignado");
        }


        TechnicianProfile technician = technicianProfileRepository.findByIdForUpdate(technicianId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "El técnico asignado no existe"));

        if (reviewRepository.existsByJobOfferId(jobOfferId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Este trabajo ya fue calificado");
        }

        JobReview saved = reviewRepository.saveAndFlush(JobReview.builder()
                .jobOfferId(jobOfferId)
                .technicianId(technicianId)
                .rating(request.rating())
                .comment(request.comment() == null ? null : request.comment().trim())
                .build());

        technicianProfileRepository.refreshRatingSummary(technicianId);

        return toResponse(saved, offer, technician);
    }

    public JobReviewResponse getReviewByJobOffer(UUID jobOfferId) {
        return reviewRepository.findByJobOfferId(jobOfferId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Este trabajo aún no tiene reseña"));
    }

    public PageResponse<JobReviewResponse> getReviewsByTechnician(UUID technicianId, int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.clamp(size, 1, MAX_PAGE_SIZE),
                Sort.by("createdAt").descending());

        Page<JobReview> result = reviewRepository.findByTechnicianId(technicianId, pageable);
        return PageResponse.from(result.map(this::toResponse));
    }

    private JobReviewResponse toResponse(JobReview r) {
        return toResponse(r, r.getJobOffer(), r.getTechnician());
    }

    private JobReviewResponse toResponse(JobReview r, JobOffer offer, TechnicianProfile tech) {
        String fullLastName = (tech.getSecondLastname() == null || tech.getSecondLastname().isBlank())
                ? tech.getLastname()
                : tech.getLastname() + " " + tech.getSecondLastname();

        return new JobReviewResponse(
                r.getId(),
                r.getJobOfferId(),
                offer.getTitle(),
                r.getTechnicianId(),
                tech.getNames(),
                fullLastName,
                r.getRating(),
                r.getComment(),
                r.getCreatedAt()
        );
    }
}
