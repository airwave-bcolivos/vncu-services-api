package com.vincu.core_services.users.web;

import com.vincu.core_services.users.dto.TechnicianProfileResponse;
import com.vincu.core_services.users.service.TechnicianService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/technicians")
@RequiredArgsConstructor
public class TechnicianController {

    private final TechnicianService technicianService;

    // TODO: con JWT, usar /me y tomar el id del usuario autenticado
    @GetMapping("/{userId}")
    public ResponseEntity<TechnicianProfileResponse> getProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(technicianService.getProfile(userId));
    }
}
