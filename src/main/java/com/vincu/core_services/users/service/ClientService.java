package com.vincu.core_services.users.service;

import com.vincu.core_services.users.domain.ClientProfile;
import com.vincu.core_services.users.domain.DocumentType;
import com.vincu.core_services.users.dto.ClientProfileResponse;
import com.vincu.core_services.users.dto.ClientProfileUpdateRequest;
import com.vincu.core_services.users.repository.ClientProfileRepository;
import com.vincu.core_services.users.repository.DocumentRepository;
import com.vincu.core_services.users.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ClientService {

    private final ClientProfileRepository clientProfileRepository;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;

    public ClientService(ClientProfileRepository clientProfileRepository,
                         UserRepository userRepository, DocumentRepository documentRepository) {
        this.clientProfileRepository = clientProfileRepository;
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
    }

    public ClientProfileResponse getProfile(UUID userId) {
        return toResponse(findProfile(userId));
    }

    @Transactional
    public ClientProfileResponse updateProfile(UUID userId, ClientProfileUpdateRequest request) {
        ClientProfile profile = findProfile(userId);

        String newEmail = request.email().trim();
        String newTypeCode = request.documentTypeCode().trim();
        String newDocId = request.documentId().trim();

        DocumentType finalType = profile.getDocumentType();
        if (!finalType.getCode().equalsIgnoreCase(newTypeCode)) {
            finalType = documentRepository.findByCode(newTypeCode)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Tipo de documento no válido"));
        }

        validateDocumentFormat(finalType.getCode(), newDocId);

        boolean docIdChanged = !profile.getDocumentId().equals(newDocId);
        if (docIdChanged && clientProfileRepository.existsByDocumentId(newDocId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El documento ya está registrado");
        }

        boolean emailChanged = !profile.getUser().getEmail().equalsIgnoreCase(newEmail);
        if (emailChanged && userRepository.existsByEmail(newEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }

        profile.getUser().setEmail(newEmail);
        profile.setNames(request.names().trim());
        profile.setLastname(request.lastname().trim());
        profile.setSecondLastname(blankToNull(request.secondLastname()));
        profile.setPhoneNumber(blankToNull(request.phoneNumber()));
        profile.setAddress(blankToNull(request.address()));
        profile.setDocumentId(newDocId);
        profile.setDocumentType(finalType);

        return toResponse(profile);
    }

    private ClientProfile findProfile(UUID userId) {
        return clientProfileRepository.findWithDetailsByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "El cliente no existe"));
    }

    private ClientProfileResponse toResponse(ClientProfile p) {
        String fullLastname = p.getSecondLastname() == null
                ? p.getLastname()
                : p.getLastname() + " " + p.getSecondLastname();

        return new ClientProfileResponse(
                p.getUserId(),
                p.getNames(),
                p.getLastname(),
                p.getSecondLastname(),
                fullLastname,
                p.getUser().getEmail(),
                p.getPhoneNumber(),
                p.getAddress(),
                p.getDocumentType().getCode(),
                p.getDocumentType().getName(),
                p.getDocumentId()
        );
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private void validateDocumentFormat(String typeCode, String documentId) {
        switch (typeCode.toUpperCase()) {
            case "DNI" -> {
                if (!documentId.matches("^[0-9]{8}$"))
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "El DNI debe tener 8 dígitos");
            }
            case "CE" -> {
                if (!documentId.matches("^[A-Za-z0-9]{12}$"))
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "El carnet de extranjería debe tener 12 caracteres");
            }
            default -> { } // otros tipos: solo aplica la validación general del DTO
        }
    }
}
