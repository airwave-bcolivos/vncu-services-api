package com.vincu.core_services.jobs.domain;

import com.vincu.core_services.ubigeo.domain.UbigeoDistrict;
import com.vincu.core_services.users.domain.ClientProfile;
import com.vincu.core_services.users.domain.Specialty;
import com.vincu.core_services.users.domain.TechnicianProfile;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="job_offers")
//quite el Data por posibles errores futuros
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class JobOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", insertable = false, updatable = false)
    @ToString.Exclude
    private ClientProfile client;

    @Column(nullable = false, length = 150)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100) //traigo el enum de speciality
    private Specialty specialty;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "max_budget", nullable = false, precision = 10, scale = 2)
    private BigDecimal maxBudget;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private JobStatus status;

    @Column(name = "technician_id")
    private UUID technicianId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id", insertable = false, updatable = false)
    @ToString.Exclude
    private TechnicianProfile technician;

    @Column(name = "ubigeo_district_id", nullable = false, length = 6)
    private String ubigeoDistrictId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubigeo_district_id", insertable = false, updatable = false)
    @ToString.Exclude
    private UbigeoDistrict district;

    @Column(name = "specific_address", nullable = false)
    private String specificAddress;

    //big decimal para evitar impreciones que puede dar el double
    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = JobStatus.OPEN;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

