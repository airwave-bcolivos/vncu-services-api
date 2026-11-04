package com.vincu.core_services.jobs.service;

import com.vincu.core_services.jobs.domain.JobOffer;
import com.vincu.core_services.jobs.domain.JobStatus;
import com.vincu.core_services.jobs.dto.JobOfferDetailResponse;
import com.vincu.core_services.jobs.dto.JobOfferSummaryResponse;
import com.vincu.core_services.jobs.dto.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.vincu.core_services.jobs.dto.JobOfferRequest;
import com.vincu.core_services.jobs.dto.JobOfferResponse;
import com.vincu.core_services.jobs.repository.JobOfferRepository;
import com.vincu.core_services.users.repository.ClientProfileRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;
import com.vincu.core_services.users.domain.Specialty;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;


@Service
@Transactional(readOnly = true)
public class JobOfferService {

    private static final int MAX_PAGE_SIZE = 50;

    private final JobOfferRepository jobOfferRepository;
    private final ClientProfileRepository clientProfileRepository;

    public JobOfferService(JobOfferRepository _jobOfferRepository,
                           ClientProfileRepository _clientProfileRepository) {
        this.jobOfferRepository = _jobOfferRepository;
        this.clientProfileRepository = _clientProfileRepository;
    }

    public PageResponse<JobOfferSummaryResponse> getOpenOffers(
            Specialty specialty, String districtId, int page, int size) {

        // Base: solo ofertas abiertas
        Specification<JobOffer> spec =
                (root, query, cb) -> cb.equal(root.get("status"), JobStatus.OPEN);

        if (specialty != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("specialty"), specialty));
        }
        if (districtId != null && !districtId.isBlank()) {
            String id = districtId.trim();
            spec = spec.and((root, query, cb) -> cb.equal(root.get("ubigeoDistrictId"), id));
        }

        return PageResponse.from(
                jobOfferRepository.findAll(spec, buildPageable(page, size))
                        .map(this::toSummary)
        );
    }

    public PageResponse<JobOfferSummaryResponse> getOffersByClient(UUID clientId, int page, int size) {

        return PageResponse.from(
                jobOfferRepository.findByClientId(clientId, buildPageable(page, size))
                        .map(this::toSummary)
        );
    }

    public JobOfferDetailResponse getOfferDetail(UUID jobId, UUID viewerId) {
        JobOffer offer = jobOfferRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "La oferta no existe"));

        return toDetail(offer, viewerId);
    }

    private JobOfferSummaryResponse toSummary(JobOffer o) {
        return new JobOfferSummaryResponse(
                o.getId(),
                o.getTitle(),
                o.getSpecialty(),
                o.getMaxBudget(),
                o.getStatus(),
                o.getUbigeoDistrictId(),
                o.getCreatedAt()
        );
    }

    private JobOfferDetailResponse toDetail(JobOffer o, UUID viewerId) {
        // Regla de privacidad: la dirección exacta solo la ven
        // el dueño de la oferta o el técnico asignado cuando está IN_PROGRESS
        boolean isOwner = o.getClientId().equals(viewerId);
        boolean isAssignedTechnician = o.getStatus() == JobStatus.IN_PROGRESS
                && o.getTechnicianId() != null
                && o.getTechnicianId().equals(viewerId);

        String address = (isOwner || isAssignedTechnician) ? o.getSpecificAddress() : null;

        return new JobOfferDetailResponse(
                o.getId(),
                o.getClientId(),
                o.getTechnicianId(),
                o.getTitle(),
                o.getSpecialty(),
                o.getDescription(),
                o.getMaxBudget(),
                o.getStatus(),
                o.getUbigeoDistrictId(),
                address,
                o.getLatitude(),
                o.getLongitude(),
                o.getCreatedAt(),
                o.getUpdatedAt()
        );
    }

    @Transactional
    public JobOfferResponse createJobOffer(JobOfferRequest request) {

        if (!clientProfileRepository.existsById(request.clientId()))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El cliente no existe");


        // Uso del patrón Builder de Lombok
        JobOffer offer = JobOffer.builder()
                .clientId(request.clientId())
                .title(request.title().trim())
                .specialty(request.specialty())
                .description(request.description().trim())
                .maxBudget(request.maxBudget())
                .ubigeoDistrictId(request.ubigeoDistrictId())
                .specificAddress(request.specificAddress())
                .latitude(request.latitude())
                .longitude(request.longitude())
                //.latitude(BigDecimal.ZERO.equals(request.latitude()) ? BigDecimal.ZERO : request.latitude())
                //.longitude(BigDecimal.ZERO.equals(request.longitude()) ? BigDecimal.ZERO : request.longitude())
                // status, createdAt y updatedAt se manejan solos en los @PrePersist de la entidad
                .build();

        JobOffer savedOffer = jobOfferRepository.save(offer);

        return new JobOfferResponse(
                savedOffer.getId(),
                savedOffer.getClientId(),
                savedOffer.getTitle(),
                savedOffer.getSpecialty(),
                savedOffer.getDescription(),
                savedOffer.getMaxBudget(),
                savedOffer.getStatus(),
                savedOffer.getUbigeoDistrictId(),
                savedOffer.getLatitude(),
                savedOffer.getLongitude(),
                savedOffer.getCreatedAt()
        );
    }

     // un private reutilizable
    private Pageable buildPageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return PageRequest.of(safePage, safeSize, Sort.by("createdAt").descending());
    }
}