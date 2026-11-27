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
import com.vincu.core_services.review.mapper.ReviewMapper;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class JobReviewService {

    private static final int MAX_PAGE_SIZE = 50;

    private final ReviewMapper reviewMapper;
    private final JobReviewRepository reviewRepository;
    private final JobOfferRepository jobOfferRepository;
    private final TechnicianProfileRepository technicianProfileRepository;

    public JobReviewService(ReviewMapper reviewMapper, JobReviewRepository reviewRepository,
                            JobOfferRepository jobOfferRepository,
                            TechnicianProfileRepository technicianProfileRepository) {
        this.reviewMapper = reviewMapper;
        this.reviewRepository = reviewRepository;
        this.jobOfferRepository = jobOfferRepository;
        this.technicianProfileRepository = technicianProfileRepository;
    }


    @Transactional
    public JobReviewResponse createReview(UUID jobOfferId, UUID userId, JobReviewRequest request) {

        JobOffer offer = jobOfferRepository.findByIdForUpdate(jobOfferId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "La oferta de trabajo no existe"));

        requireAuthor(offer, userId);

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

        return reviewMapper.toJobReviewResponse(saved, offer, technician);
    }

    private static void requireAuthor(JobOffer offer, UUID userId) {
        if (!userId.equals(offer.getClientId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el cliente que publicó el trabajo puede calificarlo");
        }
    }

    public JobReviewResponse getReviewByJobOffer(UUID jobOfferId, UUID userId) {
        var offer = jobOfferRepository.findById(jobOfferId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La oferta de trabajo no existe"));
        requireAuthor(offer, userId);
        return reviewRepository.findByJobOfferId(jobOfferId)
                .map(reviewMapper::toJobReviewResponse)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Este trabajo aún no tiene reseña"));
    }

    public PageResponse<JobReviewResponse> getReviewsByTechnician(UUID technicianId, int page, int size) {
       if(!technicianProfileRepository.existsById(technicianId)) {
           throw new ResponseStatusException(
                   HttpStatus.NOT_FOUND, "El perfil de este tecnico no existe"
           );
       }

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.clamp(size, 1, MAX_PAGE_SIZE),
                Sort.by("createdAt").descending());

        Page<JobReview> result = reviewRepository.findByTechnicianId(technicianId, pageable);
        return PageResponse.from(result.map(reviewMapper::toJobReviewResponse));
    }

}
