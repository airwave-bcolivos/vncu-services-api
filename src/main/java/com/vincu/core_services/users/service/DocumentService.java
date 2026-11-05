package com.vincu.core_services.users.service;

import com.vincu.core_services.users.dto.DocumentTypeResponse;
import com.vincu.core_services.users.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class DocumentService {

    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    // Tipos de documento activos para el dropdown (registro y edición de perfil)
    public List<DocumentTypeResponse> getActiveTypes() {
        return documentRepository.findByIsActiveTrueOrderByNameAsc().stream()
                .map(t -> new DocumentTypeResponse(t.getCode(), t.getName()))
                .toList();
    }
}