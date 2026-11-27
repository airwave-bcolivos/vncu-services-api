package com.vincu.core_services.jobs.web;

import com.vincu.core_services.jobs.dto.*;
import com.vincu.core_services.jobs.domain.JobStatus;
import com.vincu.core_services.jobs.service.JobOfferService;
import com.vincu.core_services.users.domain.Specialty;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobOfferController {

    private final JobOfferService jobOfferService;

    public JobOfferController(JobOfferService jobOfferService) {
        this.jobOfferService = jobOfferService;
    }


    @GetMapping("/open")
    public ResponseEntity<PageResponse<JobOfferSummaryResponse>> getOpenOffers(
            @RequestParam(required = false) Specialty specialty,
            @RequestParam(required = false) String districtId,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(jobOfferService.getOpenOffers(specialty, districtId, minRating ,page, size));
    }

    @GetMapping
    public ResponseEntity<PageResponse<JobOfferSummaryResponse>> getOffersByClient(
            @RequestParam String clientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            @RequestParam(required = false) Specialty specialty,
            @RequestParam(required = false) BigDecimal minBudget,
            @RequestParam(required = false) BigDecimal maxBudget,
            @RequestParam(required = false) JobStatus status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(jobOfferService.getOffersByClient(
                clientId, createdFrom, createdTo, specialty, minBudget, maxBudget,
                status, sortBy, sortDirection, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobOfferDetailResponse> getOfferDetail(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt user) {
        UUID viewerId = user == null ? null : UUID.fromString(user.getSubject());
        boolean technician = user != null && "TECHNICIAN".equals(user.getClaimAsString("role"));
        return ResponseEntity.ok(jobOfferService.getOfferDetail(id, viewerId, technician));
    }

    @GetMapping("/assigned")
    public ResponseEntity<PageResponse<JobOfferSummaryResponse>> getOffersByTechnician(
            @RequestParam String technicianId, // TODO: tomarlo del usuario autenticado con JWT
            @RequestParam(required = false) JobStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                jobOfferService.getOffersByTechnician(technicianId, status, page, size)
        );
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<Void> completeJob(@PathVariable UUID id, @AuthenticationPrincipal Jwt user) {
        jobOfferService.completeJob(id, UUID.fromString(user.getSubject()));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelOffer(@PathVariable UUID id, @AuthenticationPrincipal Jwt user) {
        jobOfferService.cancelOffer(id, UUID.fromString(user.getSubject()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<JobOfferResponse> createOffer(@Valid @RequestBody JobOfferRequest request) {
        JobOfferResponse response = jobOfferService.createJobOffer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
