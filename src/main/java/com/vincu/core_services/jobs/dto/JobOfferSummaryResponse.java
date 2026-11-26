package com.vincu.core_services.jobs.dto;

import com.vincu.core_services.jobs.domain.JobStatus;
import com.vincu.core_services.users.domain.Specialty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record JobOfferSummaryResponse(
        String id,
        String title,
        Specialty specialty,
        BigDecimal maxBudget,
        JobStatus status,
        String ubigeoDistrictId,
        String ubigeoDistrictName,
        BigDecimal clientAverageRating,
        Integer clientRatingCount,
        LocalDateTime createdAt,
        Long propuestasCount
) { }
