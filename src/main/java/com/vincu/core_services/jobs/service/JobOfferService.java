package com.vincu.core_services.jobs.service;

import com.vincu.core_services.jobs.domain.JobOffer;
import com.vincu.core_services.jobs.domain.JobStatus;
import com.vincu.core_services.jobs.dto.*;
import com.vincu.core_services.jobs.repository.JobOfferRepository;
import com.vincu.core_services.ubigeo.repository.UbigeoDistrictRepository;
import com.vincu.core_services.users.domain.ClientProfile;
import com.vincu.core_services.users.domain.Specialty;
import com.vincu.core_services.users.domain.TechnicianProfile;
import com.vincu.core_services.users.repository.ClientProfileRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class JobOfferService {

    private static final int MAX_PAGE_SIZE = 50;

    private final JobOfferRepository jobOfferRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final UbigeoDistrictRepository ubigeoDistrictRepository;

    public JobOfferService(JobOfferRepository jobOfferRepository,
                           ClientProfileRepository clientProfileRepository,
                           UbigeoDistrictRepository ubigeoDistrictRepository) {
        this.jobOfferRepository = jobOfferRepository;
        this.clientProfileRepository = clientProfileRepository;
        this.ubigeoDistrictRepository = ubigeoDistrictRepository;
    }

    public PageResponse<JobOfferSummaryResponse> getOpenOffers(
            Specialty specialty, String districtId, int page, int size) {

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

    public PageResponse<JobOfferSummaryResponse> getOffersByClient(
            String clientId, LocalDate createdFrom, LocalDate createdTo, Specialty specialty,
            BigDecimal minBudget, BigDecimal maxBudget, JobStatus status,
            String sortBy, Sort.Direction sortDirection, int page, int size) {
        if (createdFrom != null && createdTo != null && createdFrom.isAfter(createdTo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "createdFrom no puede ser posterior a createdTo");
        }
        if ((minBudget != null && minBudget.signum() < 0)
                || (maxBudget != null && maxBudget.signum() < 0)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Los presupuestos no pueden ser negativos");
        }
        if (minBudget != null && maxBudget != null && minBudget.compareTo(maxBudget) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "minBudget no puede ser mayor que maxBudget");
        }
        if (!"createdAt".equals(sortBy) && !"maxBudget".equals(sortBy)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "sortBy debe ser createdAt o maxBudget");
        }

        UUID clientUuid = UUID.fromString(clientId.trim());
        Specification<JobOffer> spec =
                (root, query, cb) -> cb.equal(root.get("clientId"), clientUuid);
        if (createdFrom != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom.atStartOfDay()));
        }
        if (createdTo != null) {
            // Exclusive upper bound includes every instant of the requested final day.
            spec = spec.and((root, query, cb) ->
                    cb.lessThan(root.get("createdAt"), createdTo.plusDays(1).atStartOfDay()));
        }
        if (specialty != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("specialty"), specialty));
        }
        if (minBudget != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("maxBudget"), minBudget));
        }
        if (maxBudget != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("maxBudget"), maxBudget));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        Sort sort = Sort.by(sortDirection, sortBy);
        if (!"createdAt".equals(sortBy)) {
            sort = sort.and(Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        sort = sort.and(Sort.by("id"));
        return PageResponse.from(
                jobOfferRepository.findAll(spec, buildPageable(page, size, sort))
                        .map(this::toSummary)
        );
    }

    public JobOfferDetailResponse getOfferDetail(UUID jobId, UUID viewerId) {
        JobOffer offer = jobOfferRepository.findWithDetailsById(jobId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "La oferta no existe"));

        return toDetail(offer, viewerId);
    }

    @Transactional
    public JobOfferResponse createJobOffer(JobOfferRequest request) {

        if (!clientProfileRepository.existsById(request.clientId()))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El cliente no existe");

        if (!ubigeoDistrictRepository.existsById(request.ubigeoDistrictId()))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El distrito no existe");

        JobOffer offer = JobOffer.builder()
                .clientId(request.clientId())
                .title(request.title().trim())
                .specialty(request.specialty())
                .description(request.description().trim())
                .maxBudget(request.maxBudget())
                .ubigeoDistrictId(request.ubigeoDistrictId())
                .specificAddress(request.specificAddress().trim())
                .latitude(BigDecimal.ZERO) //TODO: reemplazar con logica de api para obtener latitud
                .longitude(BigDecimal.ZERO) //TODO: reemplazar con logica de api para obtener longitud
                .build();

        JobOffer saved = jobOfferRepository.save(offer);

        return new JobOfferResponse(
                saved.getId(),
                saved.getClientId(),
                saved.getTitle(),
                saved.getSpecialty(),
                saved.getDescription(),
                saved.getMaxBudget(),
                saved.getStatus(),
                saved.getUbigeoDistrictId(),
                saved.getLatitude(),
                saved.getLongitude(),
                saved.getCreatedAt()
        );
    }

    private JobOfferSummaryResponse toSummary(JobOffer o) {
        return new JobOfferSummaryResponse(
                o.getId().toString(),
                o.getTitle(),
                o.getSpecialty(),
                o.getMaxBudget(),
                o.getStatus(),
                o.getUbigeoDistrictId(),
                districtName(o),
                o.getCreatedAt()
        );
    }

    private JobOfferDetailResponse toDetail(JobOffer o, UUID viewerId) {
        boolean isOwner = o.getClientId().equals(viewerId);
        boolean isAssignedTechnician = o.getStatus() == JobStatus.IN_PROGRESS
                && o.getTechnicianId() != null
                && o.getTechnicianId().equals(viewerId);

        boolean canSeePrivateData = isOwner || isAssignedTechnician;

        String address = canSeePrivateData ? o.getSpecificAddress() : null;
        String clientName = canSeePrivateData ? fullName(o.getClient()) : null;
        String technicianName = o.getTechnician() != null ? fullName(o.getTechnician()) : null;

        return new JobOfferDetailResponse(
                o.getId(),
                o.getClientId(),
                clientName,
                o.getTechnicianId(),
                technicianName,
                o.getTitle(),
                o.getSpecialty(),
                o.getDescription(),
                o.getMaxBudget(),
                o.getStatus(),
                o.getUbigeoDistrictId(),
                districtName(o),
                address,
                o.getLatitude(),
                o.getLongitude(),
                o.getCreatedAt(),
                o.getUpdatedAt()
        );
    }

    private String fullName(ClientProfile c) {
        return c == null ? null : c.getNames() + " " + c.getLastname();
    }

    private String fullName(TechnicianProfile t) {
        return t == null ? null : t.getNames() + " " + t.getLastname();
    }

    private String districtName(JobOffer o) {
        return o.getDistrict() != null ? o.getDistrict().getName() : null;
    }

    private Pageable buildPageable(int page, int size) {
        return buildPageable(page, size, Sort.by("createdAt").descending());
    }

    private Pageable buildPageable(int page, int size, Sort sort) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return PageRequest.of(safePage, safeSize, sort);
    }
}
