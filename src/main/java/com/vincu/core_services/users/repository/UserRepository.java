package com.vincu.core_services.users.repository;

import com.vincu.core_services.users.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    // Spring Data genera automáticamente la consulta SQL para validar si el correo ya existe
    boolean existsByEmail(String email);

    @Query("""
            select u.id as id, u.email as email, u.password as password,
                   u.role as role, u.isActive as active
            from User u
            where u.email = :email
            """)
    Optional<LoginCredentials> findLoginCredentialsByEmail(String email);

    interface LoginCredentials {
        UUID getId();
        String getEmail();
        String getPassword();
        String getRole();
        Boolean getActive();
    }
}
