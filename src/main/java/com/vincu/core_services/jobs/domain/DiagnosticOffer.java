package com.vincu.core_services.jobs.domain;

import com.vincu.core_services.users.domain.TechnicianProfile;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "diagnostic_offers", uniqueConstraints = @UniqueConstraint(
        name = "uq_job_technician_offer", columnNames = {"job_offer_id", "technician_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiagnosticOffer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_offer_id", nullable = false)
    private JobOffer jobOffer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "technician_id", nullable = false)
    private TechnicianProfile technician;

    @Column(name = "diagnostic_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal diagnosticPrice;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DiagnosticOfferStatus status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public DiagnosticOffer(JobOffer jobOffer, TechnicianProfile technician, BigDecimal diagnosticPrice,
                           String description) {
        this.jobOffer = jobOffer;
        this.technician = technician;
        this.diagnosticPrice = diagnosticPrice;
        this.description = description;
        this.status = DiagnosticOfferStatus.PENDING;
    }

    public void accept() {
        status = DiagnosticOfferStatus.ACCEPTED;
    }

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
