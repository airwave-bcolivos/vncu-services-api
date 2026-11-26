package com.vincu.core_services.review.web;

import com.vincu.core_services.jobs.dto.PageResponse;
import com.vincu.core_services.review.dto.ClientReviewRequest;

import com.vincu.core_services.review.dto.ClientReviewResponse;
import com.vincu.core_services.review.service.ClientReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;


import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ClientReviewController {

    private final ClientReviewService clientReviewService;


    public ClientReviewController(ClientReviewService clientReviewService) {
        this.clientReviewService = clientReviewService;
    }

    @PostMapping("/client/{jobOfferId}/client-review")
    public ResponseEntity<ClientReviewResponse> createReview(
            @PathVariable UUID jobOfferId,
            @AuthenticationPrincipal Jwt user,
            @Valid @RequestBody ClientReviewRequest request) {
        ClientReviewResponse response = clientReviewService.createClientReview(jobOfferId, UUID.fromString(user.getSubject()), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/client/{jobOfferId}/client-review")
    public ResponseEntity<ClientReviewResponse> getReviewByJobOffer(
            @PathVariable UUID jobOfferId, @AuthenticationPrincipal Jwt user) {
        return ResponseEntity.ok(clientReviewService.getReviewByJobOffer(jobOfferId, UUID.fromString(user.getSubject())));
    }

    // Reseñas de un cliente
    @GetMapping("/clients/{clientId}/reviews")
    public ResponseEntity<PageResponse<ClientReviewResponse>> getReviewsByClient(
            @PathVariable UUID clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(clientReviewService.getReviewsByClient(clientId, page, size));
    }
}
