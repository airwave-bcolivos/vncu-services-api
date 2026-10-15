package com.vincu.core_services.users.service;

import com.vincu.core_services.users.domain.ClientProfile;
import com.vincu.core_services.users.domain.DocumentType;
import com.vincu.core_services.users.domain.TechnicianProfile;
import com.vincu.core_services.users.domain.User;
import com.vincu.core_services.users.dto.ClientRegisterRequest;
import com.vincu.core_services.users.dto.TechnicianRegisterRequest;
import com.vincu.core_services.users.repository.ClientProfileRepository;
import com.vincu.core_services.users.repository.DocumentTypeRepository;
import com.vincu.core_services.users.repository.TechnicianProfileRepository;
import com.vincu.core_services.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final TechnicianProfileRepository technicianProfileRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void registerClient(ClientRegisterRequest request) {
        validateEmail(request.email());
        DocumentType docType = getDocumentType(request.documentTypeCode());

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole("CLIENT");
        //user = userRepository.save(user);

        ClientProfile profile = new ClientProfile();
        profile.setUser(user);
        profile.setNames(request.names());
        profile.setLastname(request.lastname());
        profile.setSecondLastname(request.secondLastname());
        profile.setDocumentType(docType);
        profile.setDocumentId(request.documentId());
        profile.setPhoneNumber(request.phoneNumber());
        clientProfileRepository.save(profile);
    }

    @Transactional
    public void registerTechnician(TechnicianRegisterRequest request) {
        // 1. Reutilizamos las validaciones privadas
        validateEmail(request.email());
        DocumentType docType = getDocumentType(request.documentTypeCode());

        // 2. Crear usuario base con rol específico
        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole("TECHNICIAN"); // Rol estricto para el maestro
        user = userRepository.save(user);

        // 3. Crear el perfil del técnico
        TechnicianProfile profile = new TechnicianProfile();
        profile.setUser(user);
        profile.setNames(request.names());
        profile.setLastname(request.lastname());
        profile.setSecondLastname(request.secondLastname());
        profile.setDocumentType(docType);
        profile.setDocumentId(request.documentId());
        profile.setSpecialty(request.specialty()); // Campo exclusivo
        profile.setBioDescription(request.bioDescription()); // Campo exclusivo

        technicianProfileRepository.save(profile);
    }

    private void validateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("El correo ya se encuentra registrado.");
        }
    }

    private DocumentType getDocumentType(String documentTypeCode) {
        return documentTypeRepository.findByCode(documentTypeCode)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de documento no válido."));
    }
}
