package com.vincu.core_services.jobs.dto;

import com.vincu.core_services.jobs.domain.JobStatus;
import com.vincu.core_services.users.domain.Specialty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record JobOfferDetailResponse(
        UUID id,
        UUID clientId,
        String clientName,
        UUID technicianId,
        String technicianName,
        String title,
        Specialty specialty,
        String description,
        BigDecimal maxBudget,
        JobStatus status,
        String ubigeoDistrictId,
        String ubigeoDistrictName,
        String specificAddress, // null si el que consulta no tiene permiso
        BigDecimal latitude,
        BigDecimal longitude,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) { }
