package com.vincu.core_services.review.web;

import com.vincu.core_services.jobs.dto.PageResponse;
import com.vincu.core_services.review.dto.JobReviewRequest;
import com.vincu.core_services.review.dto.JobReviewResponse;
import com.vincu.core_services.review.service.JobReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class JobReviewController {

    private final JobReviewService reviewService;

    public JobReviewController(JobReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // Calificar un trabajo
    @PostMapping("/jobs/{jobOfferId}/review")
    public ResponseEntity<JobReviewResponse> createReview(
            @PathVariable UUID jobOfferId,
            @AuthenticationPrincipal Jwt user,
            @Valid @RequestBody JobReviewRequest request) {
        JobReviewResponse response = reviewService.createReview(jobOfferId, UUID.fromString(user.getSubject()), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Ver la reseña de un trabajo (404 si aún no existe)
    @GetMapping("/jobs/{jobOfferId}/review")
    public ResponseEntity<JobReviewResponse> getReviewByJobOffer(
            @PathVariable UUID jobOfferId, @AuthenticationPrincipal Jwt user) {
        return ResponseEntity.ok(reviewService.getReviewByJobOffer(jobOfferId, UUID.fromString(user.getSubject())));
    }

    // Reseñas de un técnico
    @GetMapping("/technicians/{technicianId}/reviews")
    public ResponseEntity<PageResponse<JobReviewResponse>> getReviewsByTechnician(
            @PathVariable UUID technicianId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.getReviewsByTechnician(technicianId, page, size));
    }
}
