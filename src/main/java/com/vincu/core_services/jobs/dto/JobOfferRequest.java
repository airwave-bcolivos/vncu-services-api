package com.vincu.core_services.jobs.dto;

import com.vincu.core_services.users.domain.Specialty;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record JobOfferRequest(
        @NotNull(message = "El cliente es obligatorio")
        UUID clientId, // TODO: reemplazar con lectura de client id desde token jwt

        @NotBlank(message = "El título es obligatorio")
        @Size(max = 150, message = "El título no puede superar los 150 caracteres")
        String title,

        @NotNull(message = "El oficio es obligatorio")
        Specialty specialty,

        @NotBlank(message = "La descripción es obligatoria")
        String description,

        @NotNull(message = "El presupuesto máximo es obligatorio")
        @DecimalMin(value = "1.00", message = "El presupuesto debe ser mayor a 0")
        @Digits(integer = 8, fraction = 2, message = "Presupuesto inválido (máx. 8 enteros y 2 decimales)") //TODO: definir presupuesto maximo
        BigDecimal maxBudget,

        @NotBlank(message = "El distrito es obligatorio")
        @Size(min = 6, max = 6, message = "El código de distrito debe tener 6 caracteres")
        String ubigeoDistrictId,

        @NotBlank(message = "La dirección es obligatoria")
        String specificAddress,

        @NotNull(message = "La latitud es obligatoria")
        @DecimalMin(value = "-90.0", message = "Latitud inválida")
        @DecimalMax(value = "90.0", message = "Latitud inválida")
        BigDecimal latitude,

        @NotNull(message = "La longitud es obligatoria")
        @DecimalMin(value = "-180.0", message = "Longitud inválida")
        @DecimalMax(value = "180.0", message = "Longitud inválida")
        BigDecimal longitude
) {}