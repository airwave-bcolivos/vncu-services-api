package com.vincu.core_services.review.service;

import com.vincu.core_services.jobs.domain.JobOffer;
import com.vincu.core_services.jobs.domain.JobStatus;
import com.vincu.core_services.jobs.dto.PageResponse;
import com.vincu.core_services.jobs.repository.JobOfferRepository;
import com.vincu.core_services.review.domain.ClientReview;
import com.vincu.core_services.review.dto.ClientReviewRequest;
import com.vincu.core_services.review.dto.ClientReviewResponse;
import com.vincu.core_services.review.mapper.ReviewMapper;
import com.vincu.core_services.review.repository.ClientReviewRepository;
import com.vincu.core_services.users.domain.ClientProfile;
import com.vincu.core_services.users.repository.ClientProfileRepository;
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
@Transactional(readOnly=true)
public class ClientReviewService {

    private static final int MAX_PAGE_SIZE = 50;

    private final ReviewMapper reviewMapper;
    private final ClientProfileRepository clientProfileRepository;
    private final JobOfferRepository offerRepository;
    private final ClientReviewRepository clientReviewRepository;


    public ClientReviewService(ReviewMapper reviewMapper, ClientProfileRepository clientProfileRepository,
                               JobOfferRepository offerRepository,
                               ClientReviewRepository clientReviewRepository) {
        this.reviewMapper = reviewMapper;
        this.clientProfileRepository = clientProfileRepository;
        this.offerRepository = offerRepository;
        this.clientReviewRepository = clientReviewRepository;
    }

    @Transactional
    public ClientReviewResponse createClientReview(UUID jobOfferId, UUID userId, ClientReviewRequest request) {

        JobOffer offer = offerRepository.findByIdForUpdate(jobOfferId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "La oferta de trabajo no existe"));

        // Solo el técnico asignado a la oferta puede calificar al cliente
        requireAuthor(offer, userId);

        if (offer.getStatus() != JobStatus.COMPLETED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Solo se pueden calificar trabajos finalizados");
        }

        UUID clientId = offer.getClientId();
        if (clientId == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "El trabajo no tiene un cliente asociado");
        }

        // Bloquea la fila del cliente para evitar condiciones de carrera al recalcular
        ClientProfile client = clientProfileRepository.findByIdForUpdate(clientId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "El perfil del cliente no existe"));

        // Una sola reseña por trabajo
        if (clientReviewRepository.existsByJobOfferId(jobOfferId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "El cliente ya fue calificado por este trabajo");
        }

        ClientReview saved = clientReviewRepository.saveAndFlush(ClientReview.builder()
                .jobOfferId(jobOfferId)
                .clientId(clientId)
                .rating(request.rating())
                .comment(request.comment() == null ? null : request.comment().trim())
                .build());

        // Recalcula promedio y cantidad en client_profiles
        clientProfileRepository.refreshRatingSummary(clientId);

        return reviewMapper.toClientReviewResponse(saved, offer, client);
    }

    private static void requireAuthor(JobOffer offer, UUID userId) {
        if (!userId.equals(offer.getTechnicianId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el técnico asignado a este trabajo puede calificar al cliente");
        }
    }

    public ClientReviewResponse getReviewByJobOffer(UUID jobOfferId, UUID userId) {
        var offer = offerRepository.findById(jobOfferId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La oferta de trabajo no existe"));
        requireAuthor(offer, userId);
        return clientReviewRepository.findByJobOfferId(jobOfferId)
                .map(reviewMapper::toClientReviewResponse)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Aún no existe una reseña del cliente para este trabajo"));
    }

    public PageResponse<ClientReviewResponse> getReviewsByClient(UUID clientId, int page, int size) {
        if (!clientProfileRepository.existsById(clientId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "El perfil del cliente no existe");
        }

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.clamp(size, 1, MAX_PAGE_SIZE),
                Sort.by("createdAt").descending());

        Page<ClientReview> result = clientReviewRepository.findByClientId(clientId, pageable);
        return PageResponse.from(result.map(reviewMapper::toClientReviewResponse));
    }

}
