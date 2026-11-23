package com.vincu.core_services.users.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TechnicianFeeResponse (
        UUID userId,
        BigDecimal consultationFee,
        LocalDateTime updatedAt
) { }