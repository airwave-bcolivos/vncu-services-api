package com.vincu.core_services.users.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "client_profiles")
@Getter
@Setter
@NoArgsConstructor
public class ClientProfile {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // Comparte la misma llave primaria del User
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 150)
    private String names;

    @Column(nullable = false, length = 100)
    private String lastname;

    @Column(name = "second_lastname", length = 100)
    private String secondLastname;

    // Relación Muchos a Uno con el catálogo de tipos de documento
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_type_id", nullable = false)
    private DocumentType documentType;

    @Column(name = "document_id", nullable = false, length = 50)
    private String documentId;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(length = 255)
    private String address;

    @Column(name = "is_active", insertable = false)
    private Boolean isActive;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;
}
