package com.vincu.core_services.users.dto;

import com.vincu.core_services.users.domain.Specialty;

import java.math.BigDecimal;
import java.util.UUID;


public record TechnicianProfileResponse(
        UUID userId,
        String names,
        String lastname,
        String secondLastname,
        String fullLastname,
        String email,
        String documentTypeCode,
        String documentTypeName,
        String documentId,
        Specialty specialty,
        String bioDescription,
        Boolean isActive,
        BigDecimal averageRating,
        Integer ratingCount,
        BigDecimal consultationFee
) { }
