package com.vincu.core_services.complaint.dto;

import com.vincu.core_services.complaint.domain.ComplaintStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ComplaintResponse(
        UUID complaintId,

        //fk
        UUID userId,
        String userName,
        String userEmail,
        String userRole,

        String title,
        String description,
        ComplaintStatus status,
        LocalDateTime createdAt
) {}
