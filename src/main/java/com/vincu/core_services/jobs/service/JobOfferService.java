package com.vincu.core_services.jobs.service;

import com.vincu.core_services.jobs.domain.JobOffer;
import com.vincu.core_services.jobs.domain.JobStatus;
import com.vincu.core_services.jobs.dto.*;
import com.vincu.core_services.jobs.mapper.JobOfferMapper;
import com.vincu.core_services.jobs.repository.DiagnosticOfferRepository;
import com.vincu.core_services.jobs.repository.JobOfferRepository;
import com.vincu.core_services.ubigeo.repository.UbigeoDistrictRepository;
import com.vincu.core_services.users.domain.ClientProfile;
import com.vincu.core_services.users.domain.Specialty;
import com.vincu.core_services.users.repository.ClientProfileRepository;
import com.vincu.core_services.users.repository.TechnicianProfileRepository;
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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.vincu.core_services.jobs.domain.DiagnosticOfferStatus.PENDING;
import static com.vincu.core_services.jobs.domain.DiagnosticOfferStatus.REJECTED;

@Service
@Transactional(readOnly = true)
public class JobOfferService {

    private static final int MAX_PAGE_SIZE = 50;

    private final JobOfferMapper jobOfferMapper;
    private final TechnicianProfileRepository techinicianRepository;
    private final JobOfferRepository jobOfferRepository;
    private final DiagnosticOfferRepository diagnosticOfferRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final UbigeoDistrictRepository ubigeoDistrictRepository;
    private final NominatimGeocodingService geocodingService;

    public JobOfferService(JobOfferMapper jobOfferMapper,
                           TechnicianProfileRepository techinicianRepository,
                           JobOfferRepository jobOfferRepository,
                           DiagnosticOfferRepository diagnosticOfferRepository,
                           ClientProfileRepository clientProfileRepository,
                           UbigeoDistrictRepository ubigeoDistrictRepository, NominatimGeocodingService geocodingService) {
        this.jobOfferMapper = jobOfferMapper;
        this.techinicianRepository = techinicianRepository;
        this.jobOfferRepository = jobOfferRepository;
        this.diagnosticOfferRepository = diagnosticOfferRepository;
        this.clientProfileRepository = clientProfileRepository;
        this.ubigeoDistrictRepository = ubigeoDistrictRepository;
        this.geocodingService = geocodingService;
    }

    public PageResponse<JobOfferSummaryResponse> getOpenOffers(
            Specialty specialty, String districtId, BigDecimal minRating, int page, int size) {

        Specification<JobOffer> spec =
                (root, query, cb) -> cb.equal(root.get("status"), JobStatus.OPEN);

        if (minRating != null
                && (minRating.compareTo(BigDecimal.ONE) < 0
                || minRating.compareTo(BigDecimal.valueOf(5)) > 0)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La calificación mínima debe estar entre 1 y 5");
        }

        if (specialty != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("specialty"), specialty));
        }
        if (districtId != null && !districtId.isBlank()) {
            String id = districtId.trim();
            spec = spec.and((root, query, cb) -> cb.equal(root.get("ubigeoDistrictId"), id));
        }
        if (minRating != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("client").get("averageRating"), minRating));
        }

        return PageResponse.from(
                jobOfferRepository.findAll(spec, buildPageable(page, size))
                        .map(jobOfferMapper::toSummary)
        );
    }

    public PageResponse<JobOfferSummaryResponse> getOffersByClient( String clientId, LocalDate createdFrom, LocalDate createdTo, Specialty specialty,
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
        var jobs = jobOfferRepository.findAll(spec, buildPageable(page, size, sort));
        if (jobs.isEmpty()) {
            return PageResponse.from(jobs.map(jobOfferMapper::toSummary));
        }
        var ids = jobs.getContent().stream().map(JobOffer::getId).toList();
        Map<UUID, Long> counts = diagnosticOfferRepository.countByJobIds(ids).stream().collect(Collectors.toMap(
                DiagnosticOfferRepository.JobOfferCount::getJobId, DiagnosticOfferRepository.JobOfferCount::getTotal));
        return PageResponse.from(jobs.map(job -> jobOfferMapper.toSummary(job, counts.getOrDefault(job.getId(), 0L))));
    }

    // Visualización de trabajos por estado del trabajo por técnico
    public PageResponse<JobOfferSummaryResponse> getOffersByTechnician(
            String technicianId,
            JobStatus status,
            int page,
            int size) {

        UUID techUuid = UUID.fromString(technicianId.trim());

        if (!techinicianRepository.existsById(techUuid)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El técnico no existe");
        }

        Specification<JobOffer> spec =
                (root, query, cb) -> cb.equal(root.get("technicianId"), techUuid);

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        return PageResponse.from(
                jobOfferRepository.findAll(spec, buildPageable(page, size))
                        .map(jobOfferMapper::toSummary)
        );
    }

    public JobOfferDetailResponse getOfferDetail(UUID jobId, UUID viewerId, boolean technician) {
        JobOffer offer = jobOfferRepository.findWithDetailsById(jobId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "La oferta no existe"));

        boolean diagnosticOfferSubmitted = technician
                && diagnosticOfferRepository.existsByJobOffer_IdAndTechnician_UserId(jobId, viewerId);
        return jobOfferMapper.toDetail(offer, viewerId, diagnosticOfferSubmitted);
    }

    @Transactional
    public void completeJob(UUID id, UUID userId) {
        JobOffer job = jobOfferRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La publicación no existe"));
        if (!userId.equals(job.getClientId()) && !userId.equals(job.getTechnicianId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (job.getTechnicianId() == null
                || (job.getStatus() != JobStatus.IN_PROGRESS && job.getStatus() != JobStatus.COMPLETED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se pueden finalizar trabajos asignados en progreso");
        }
        job.setStatus(JobStatus.COMPLETED);
    }

    @Transactional
    public void cancelOffer(UUID id, UUID clientId) {
        JobOffer offer = jobOfferRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "La oferta no existe"));
        if (!offer.getClientId().equals(clientId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        offer.setStatus(JobStatus.CANCELED);
        diagnosticOfferRepository.rejectPending(id, PENDING, REJECTED);
    }

    @Transactional
    public JobOfferResponse createJobOffer(JobOfferRequest request) {
        ClientProfile client = clientProfileRepository.findById(request.clientId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El cliente no existe"));

        var district = ubigeoDistrictRepository.findById(request.ubigeoDistrictId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El distrito no existe"));

        BigDecimal finalLat;
        BigDecimal finalLon;

        if (request.latitude() != null && request.longitude() != null) {
            finalLat = request.latitude();
            finalLon = request.longitude();
        } else {
            String departmentName = (district.getProvince() != null && district.getProvince().getDepartment() != null)
                    ? district.getProvince().getDepartment().getName()
                    : "Lima";

            var coords = geocodingService.geocodeAddress(
                    request.specificAddress().trim(),
                    district.getName(),
                    departmentName
            );
            finalLat = coords.latitude();
            finalLon = coords.longitude();
        }

        JobOffer offer = JobOffer.builder()
                .clientId(request.clientId())
                .title(request.title().trim())
                .specialty(request.specialty())
                .description(request.description().trim())
                .maxBudget(request.maxBudget())
                .ubigeoDistrictId(request.ubigeoDistrictId())
                .specificAddress(request.specificAddress().trim())
                .latitude(finalLat)
                .longitude(finalLon)
                .build();

        JobOffer saved = jobOfferRepository.save(offer);
        return jobOfferMapper.toResponse(saved, client);
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
