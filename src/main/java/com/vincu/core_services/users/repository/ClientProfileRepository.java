package com.vincu.core_services.users.repository;

import com.vincu.core_services.users.domain.ClientProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClientProfileRepository extends JpaRepository<ClientProfile, UUID> {

    @EntityGraph(attributePaths = {"user", "documentType"})
    Optional<ClientProfile> findWithDetailsByUserId(UUID userid);

    boolean existsByDocumentId(String documentId);
}
