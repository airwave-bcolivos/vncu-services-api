package com.vincu.core_services.users.web;

import com.vincu.core_services.users.dto.TechnicianFeeResponse;
import com.vincu.core_services.users.dto.TechnicianProfileResponse;
import com.vincu.core_services.users.dto.TechnicianUpdateFeeRequest;
import com.vincu.core_services.users.service.TechnicianService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PatchMapping("/{userId}/consultation-fee")
    public ResponseEntity<TechnicianFeeResponse> updateConsultationFee(
            @PathVariable UUID userId,
            @Valid @RequestBody TechnicianUpdateFeeRequest request
    ) {
        return ResponseEntity.ok(technicianService.updateConsultationFee(userId, request));
    }
}