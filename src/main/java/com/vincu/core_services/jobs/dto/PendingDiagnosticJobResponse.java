package com.vincu.core_services.jobs.dto;

import com.vincu.core_services.users.domain.Specialty;

import java.time.LocalDateTime;
import java.util.UUID;

public record PendingDiagnosticJobResponse(
        UUID id,
        String title,
        Specialty specialty,
        String description,
        LocalDateTime createdAt
) {}
