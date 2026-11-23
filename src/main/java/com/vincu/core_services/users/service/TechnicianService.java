package com.vincu.core_services.users.service;
import com.vincu.core_services.users.domain.TechnicianProfile;
import com.vincu.core_services.users.dto.TechnicianFeeResponse;
import com.vincu.core_services.users.dto.TechnicianUpdateFeeRequest;
import com.vincu.core_services.users.repository.TechnicianProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TechnicianService {

    private final TechnicianProfileRepository technicianProfileRepository;

    @Transactional
    public TechnicianFeeResponse updateConsultationFee(UUID userId, TechnicianUpdateFeeRequest request) {
        TechnicianProfile technician = technicianProfileRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Maestro de oficio no encontrado"));

        technician.setConsultationFee(request.consultationFee());
        TechnicianProfile saved = technicianProfileRepository.saveAndFlush(technician);

        return new TechnicianFeeResponse(
                saved.getUserId(),
                saved.getConsultationFee(),
                saved.getUpdatedAt()
        );
    }
}