package com.vincu.core_services.users.repository;

import com.vincu.core_services.users.domain.TechnicianProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TechnicianProfileRepository extends JpaRepository<TechnicianProfile, UUID> {
}
