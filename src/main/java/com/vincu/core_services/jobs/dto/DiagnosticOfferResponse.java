package com.vincu.core_services.jobs.dto;

import com.vincu.core_services.jobs.domain.DiagnosticOffer;
import com.vincu.core_services.jobs.domain.DiagnosticOfferStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record DiagnosticOfferResponse(
        UUID id,
        UUID jobOfferId,
        UUID technicianId,
        String technicianName,
        BigDecimal technicianAverageRating,
        Integer technicianRatingCount,
        BigDecimal diagnosticPrice,
        String description,
        DiagnosticOfferStatus status,
        LocalDateTime createdAt
) {
    public static DiagnosticOfferResponse from(DiagnosticOffer offer) {
        var technician = offer.getTechnician();
        return new DiagnosticOfferResponse(
                offer.getId(), offer.getJobOffer().getId(), technician.getUserId(),
                technician.getNames() + " " + technician.getLastname(),
                technician.getAverageRating(), technician.getRatingCount(),
                offer.getDiagnosticPrice(), offer.getDescription(), offer.getStatus(), offer.getCreatedAt());
    }
}
