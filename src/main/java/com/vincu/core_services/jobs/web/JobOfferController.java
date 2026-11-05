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


    //TODO: posible logica para listar publicaciones para el maestro
    @GetMapping("/open")
    public ResponseEntity<PageResponse<JobOfferSummaryResponse>> getOpenOffers(
            @RequestParam(required = false) Specialty specialty,
            @RequestParam(required = false) String districtId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(jobOfferService.getOpenOffers(specialty, districtId, page, size));
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
            @RequestParam(required = false) UUID viewerId) { // TODO: tomarlo del usuario autenticado
        return ResponseEntity.ok(jobOfferService.getOfferDetail(id, viewerId));
    }

    @PostMapping
    public ResponseEntity<JobOfferResponse> createOffer(@Valid @RequestBody JobOfferRequest request) {
        JobOfferResponse response = jobOfferService.createJobOffer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
