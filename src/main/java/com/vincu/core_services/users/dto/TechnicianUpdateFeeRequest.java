package com.vincu.core_services.users.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TechnicianUpdateFeeRequest(
        @NotNull(message = "El precio de consulta es obligatorio")
        @DecimalMin(value = "0.00", inclusive = true, message = "El precio de consulta no puede ser negativo")
        @Digits(integer = 8, fraction = 2, message = "El formato del monto debe tener máximo 8 enteros y 2 decimales")
        BigDecimal consultationFee
) { }