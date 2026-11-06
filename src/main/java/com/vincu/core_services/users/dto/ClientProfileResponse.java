package com.vincu.core_services.users.dto;

import java.util.UUID;

public record ClientProfileResponse(
        UUID userId,
        String names,
        String lastname,
        String secondLastname,
        String fullLastname, //esto sera la concatenacion de los dos apellidos,
        String email,
        String phoneNumber,
        String address,
        String documentTypeCode, //fk
        String documentType,
        String documentId
) { }
