package com.vincu.core_services.review.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record JobReviewResponse(
        UUID reviewId,

        //fk ofertas de trabajo
        UUID jobOfferId,
        String jobTitle,

        //fk tecnicos
        UUID technicianId,
        String technicianName,
        String technicianLastName,

        Integer rating,
        String comment,
        LocalDateTime createdAt
) { }
