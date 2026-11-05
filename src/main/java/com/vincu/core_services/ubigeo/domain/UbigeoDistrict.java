package com.vincu.core_services.ubigeo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ubigeo_districts")
@Getter
@NoArgsConstructor
public class UbigeoDistrict {

    @Id
    @Column(length = 6)
    private String id;

    @Column(name = "province_id", nullable = false, length = 4)
    private String provinceId;

    @Column(nullable = false, length = 100)
    private String name;
}