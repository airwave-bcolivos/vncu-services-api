package com.vincu.core_services.jobs.web;

import com.vincu.core_services.jobs.dto.DiagnosticOfferRequest;
import com.vincu.core_services.jobs.dto.DiagnosticOfferResponse;
import com.vincu.core_services.jobs.dto.PageResponse;
import com.vincu.core_services.jobs.dto.PendingDiagnosticJobResponse;
import com.vincu.core_services.jobs.service.DiagnosticOfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/jobs")
public class DiagnosticOfferController {
    private final DiagnosticOfferService service;

    @GetMapping("/diagnostic-offers/mine")
    public PageResponse<PendingDiagnosticJobResponse> listPendingJobs(
            @AuthenticationPrincipal Jwt user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return service.listPendingJobs(UUID.fromString(user.getSubject()), page, size);
    }

    @PostMapping("/{jobId}/diagnostic-offers")
    @ResponseStatus(HttpStatus.CREATED)
    public DiagnosticOfferResponse create(@PathVariable UUID jobId,
                                          @AuthenticationPrincipal Jwt user,
                                          @Valid @RequestBody DiagnosticOfferRequest request) {
        return service.create(jobId, UUID.fromString(user.getSubject()), request);
    }

    @GetMapping("/{jobId}/diagnostic-offers")
    public List<DiagnosticOfferResponse> list(@PathVariable UUID jobId, @AuthenticationPrincipal Jwt user) {
        return service.list(jobId, UUID.fromString(user.getSubject()));
    }

    @PatchMapping("/{jobId}/diagnostic-offers/{offerId}/accept")
    public DiagnosticOfferResponse accept(@PathVariable UUID jobId, @PathVariable UUID offerId,
                                          @AuthenticationPrincipal Jwt user) {
        return service.accept(jobId, offerId, UUID.fromString(user.getSubject()));
    }
}
