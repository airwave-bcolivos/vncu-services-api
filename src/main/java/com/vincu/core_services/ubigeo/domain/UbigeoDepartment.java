package com.vincu.core_services.ubigeo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ubigeo_departments")
@Getter
@NoArgsConstructor
public class UbigeoDepartment {

    @Id
    @Column(length = 2)
    private String id;

    @Column(nullable = false, length = 100)
    private String name;
}
