package com.vincu.core_services.jobs.dto;

import com.vincu.core_services.jobs.domain.JobStatus;
import com.vincu.core_services.users.domain.Specialty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record JobOfferDetailResponse(
        UUID id,
        UUID clientId,
        UUID technicianId,
        String title,
        Specialty specialty,
        String description,
        BigDecimal maxBudget,
        JobStatus status,
        String ubigeoDistrictId,
        String specificAddress, // null si el que consulta no tiene permiso
        BigDecimal latitude,
        BigDecimal longitude,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) { }
