package com.vincu.core_services.users.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "document_type")
@Getter
@Setter
@NoArgsConstructor
public class DocumentType {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "document_type_id")
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 10, unique = true)
    private String code;

    // insertable = false porque la base de datos ya le pone 'true' por defecto
    @Column(name = "is_active", insertable = false)
    private Boolean isActive;
}
