package com.vincu.core_services.users.dto;

public record LoginResponse(
        String id,
        String email,
        String role,
        String token
) {
}
