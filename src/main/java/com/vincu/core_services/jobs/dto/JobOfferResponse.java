package com.vincu.core_services.jobs.dto;

import com.vincu.core_services.jobs.domain.JobStatus;
import com.vincu.core_services.users.domain.Specialty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record JobOfferResponse(
        UUID id,
        UUID clientId,
        String title,
        Specialty specialty,
        String description,
        BigDecimal maxBudget,
        JobStatus status,
        String ubigeoDistrictId,
        BigDecimal latitude,
        BigDecimal longitude,
        LocalDateTime createdAt
) {}