package com.vincu.core_services.jobs.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record DiagnosticOfferRequest(
        @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal diagnosticPrice,
        @NotBlank String description
) {}
