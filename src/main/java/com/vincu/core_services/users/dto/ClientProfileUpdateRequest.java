package com.vincu.core_services.users.dto;

import jakarta.validation.constraints.*;

public record ClientProfileUpdateRequest(

        @NotBlank(message = "Los nombres son obligatorios")
        @Size(max = 150, message = "Maximo 150 caracteres")
        String names,

        @NotBlank(message = "El apellido paterno es obligatorio")
        @Size(max = 100, message = "Maximo 100 caracteres")
        String lastname,

        @Size(max = 100, message = "Maximo 100 caracteres")
        String secondLastname,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Correo en formato invalido")
        @Size(max = 255, message = "El email solo puede tener como maximo 255 caracteres")
        String email,

        @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Teléfono inválido")
        String phoneNumber,

        @Size(max = 255, message = "La direccion no puede pasar de los 255 caracteres")
        String address,

        @NotBlank(message = "El tipo de documento es obligatorio")
        String documentTypeCode,

        @NotBlank(message = "El número de documento es obligatorio")
        @Pattern(regexp = "^[A-Za-z0-9]{8,12}$",
                message = "El número de documento debe ser alfanumérico (8 a 12 caracteres)")
        String documentId




) { }
