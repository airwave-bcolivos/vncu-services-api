package com.vincu.core_services.users.repository;

import com.vincu.core_services.users.domain.ClientProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClientProfileRepository extends JpaRepository<ClientProfile, UUID> {
}
