package com.vincu.core_services.users.web;

import com.vincu.core_services.users.dto.ClientProfileResponse;
import com.vincu.core_services.users.dto.ClientProfileUpdateRequest;
import com.vincu.core_services.users.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
@RestController
@RequestMapping("/api/v1/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    // TODO: con JWT, usar /me y tomar el id del usuario autenticado
    @GetMapping("/{userId}")
    public ResponseEntity<ClientProfileResponse> getProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(clientService.getProfile(userId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ClientProfileResponse> updateProfile(
            @PathVariable UUID userId,
            @Valid @RequestBody ClientProfileUpdateRequest request) {
        return ResponseEntity.ok(clientService.updateProfile(userId, request));
    }
}