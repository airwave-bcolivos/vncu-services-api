package com.vincu.core_services.users.service;

import com.vincu.core_services.users.domain.TechnicianProfile;
import com.vincu.core_services.users.dto.TechnicianProfileResponse;
import com.vincu.core_services.users.repository.TechnicianProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TechnicianService {

    private final TechnicianProfileRepository technicianProfileRepository;

    public TechnicianProfileResponse getProfile(UUID userId) {
        return toResponse(findProfile(userId));
    }

    private TechnicianProfile findProfile(UUID userId) {
        return technicianProfileRepository.findWithDetailsByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "El maestro de oficio no existe"));
    }

    private TechnicianProfileResponse toResponse(TechnicianProfile p) {
        String fullLastname = p.getSecondLastname() == null
                ? p.getLastname()
                : p.getLastname() + " " + p.getSecondLastname();

        return new TechnicianProfileResponse(
                p.getUserId(),
                p.getNames(),
                p.getLastname(),
                p.getSecondLastname(),
                fullLastname,
                p.getUser().getEmail(),
                p.getDocumentType().getCode(),
                p.getDocumentType().getName(),
                p.getDocumentId(),
                p.getSpecialty(),
                p.getBioDescription(),
                p.getIsActive(),
                p.getAverageRating(),
                p.getRatingCount()
        );
    }
}
