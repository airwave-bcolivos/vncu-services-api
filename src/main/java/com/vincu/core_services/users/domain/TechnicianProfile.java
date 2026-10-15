package com.vincu.core_services.users.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "technician_profiles")
@Getter
@Setter
@NoArgsConstructor
public class TechnicianProfile {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 150)
    private String names;

    @Column(nullable = false, length = 100)
    private String lastname;

    @Column(name = "second_lastname", length = 100)
    private String secondLastname;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_type_id", nullable = false)
    private DocumentType documentType;

    @Column(name = "document_id", nullable = false, length = 50)
    private String documentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private Specialty specialty;

    @Column(name = "bio_description", columnDefinition = "TEXT")
    private String bioDescription;

    @Column(name = "is_active", insertable = false)
    private Boolean isActive;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;
}
