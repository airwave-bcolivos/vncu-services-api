package com.vincu.core_services.ubigeo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ubigeo_provinces")
@Getter
@NoArgsConstructor
public class UbigeoProvince {

    @Id
    @Column(length = 4)
    private String id;

    @Column(name = "department_id", nullable = false, length = 2)
    private String departmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", insertable = false, updatable = false)
    private UbigeoDepartment department;

    @Column(nullable = false, length = 100)
    private String name;
}
