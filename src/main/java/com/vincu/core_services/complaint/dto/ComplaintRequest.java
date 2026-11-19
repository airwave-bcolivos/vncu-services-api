package com.vincu.core_services.complaint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ComplaintRequest(

        @NotNull(message = "El usuario es obligatorio")
        UUID userId, //TODO: luego vendrá del usuario autenticado

        @NotBlank(message = "El título del reclamo es obligatorio")
        @Size(max = 150, message = "El título no puede superar los 150 caracteres")
        String title,

        @NotBlank(message = "La descripción del reclamo es obligatoria")
        @Size(max = 300, message = "La descripción no puede superar los 300 caracteres")
        String description
) {}
