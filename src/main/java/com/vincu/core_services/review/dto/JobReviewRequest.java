package com.vincu.core_services.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record JobReviewRequest(
        //TODO: Quien califica. Más adelante saldrá del usuario autenticado
        @NotNull(message = "El cliente es obligatorio")
        UUID clientId,

        @NotNull(message = "La puntuación es obligatoria")
        @Min(value = 1, message = "La puntuación mínima es 1")
        @Max(value = 5, message = "La puntuación máxima es 5")
        Integer rating,

        @Size(max = 1000, message = "La reseña no puede superar los 1000 caracteres")
        String comment
) { }