package com.vincu.core_services.users.dto;

import com.vincu.core_services.users.domain.Specialty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TechnicianRegisterRequest(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Formato de correo inválido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        String password,

        @NotBlank(message = "Los nombres son obligatorios")
        String names,

        @NotBlank(message = "El apellido paterno es obligatorio")
        String lastname,

        String secondLastname,

        @NotNull(message = "El tipo de documento es obligatorio")
        String documentTypeCode,

        @NotBlank(message = "El número de documento es obligatorio")
        String documentId,

        @NotNull(message = "La especialidad es obligatoria y debe ser válida")
        Specialty specialty,

        @NotBlank(message = "El número de teléfono es obligatorio")
        String bioDescription
) {}
