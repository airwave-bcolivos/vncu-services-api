package com.vincu.core_services.jobs.web;

import com.vincu.core_services.jobs.dto.JobOfferRequest;
import com.vincu.core_services.jobs.dto.JobOfferResponse;
import com.vincu.core_services.jobs.service.JobOfferService;
import com.vincu.core_services.users.domain.Specialty;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.vincu.core_services.jobs.dto.JobOfferDetailResponse;
import com.vincu.core_services.jobs.dto.JobOfferSummaryResponse;
import com.vincu.core_services.jobs.dto.PageResponse;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobOfferController {

    private final JobOfferService jobOfferService;

    public JobOfferController(JobOfferService jobOfferService) {
        this.jobOfferService = jobOfferService;
    }


    @GetMapping
    public ResponseEntity<PageResponse<JobOfferSummaryResponse>> getOffersByClient(
            @RequestParam String clientId, // TODO: tomarlo del usuario autenticado
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(jobOfferService.getOffersByClient(clientId, page, size));
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
