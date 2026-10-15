package com.vincu.core_services.users.repository;

import com.vincu.core_services.users.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    // Spring Data genera automáticamente la consulta SQL para validar si el correo ya existe
    boolean existsByEmail(String email);
}
