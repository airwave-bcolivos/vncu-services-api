package com.vincu.core_services.review.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClientReviewResponse(
        UUID reviewId,

        //fk ofertas de trabajo
        UUID jobOfferId,
        String jobTitle,

        //fk clientes
        UUID clientId,
        String clientName,
        String clientLastName,

        Integer rating,
        String comment,
        LocalDateTime createdAt
) {}
