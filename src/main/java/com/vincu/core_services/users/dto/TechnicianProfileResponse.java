package com.vincu.core_services.users.dto;

import com.vincu.core_services.users.domain.Specialty;

import java.math.BigDecimal;
import java.util.UUID;

public record TechnicianProfileResponse(
        UUID userId,
        String names,
        String lastname,
        String secondLastname,
        Specialty specialty,
        String bioDescription,
        BigDecimal averageRating,
        Integer ratingCount
) { }
