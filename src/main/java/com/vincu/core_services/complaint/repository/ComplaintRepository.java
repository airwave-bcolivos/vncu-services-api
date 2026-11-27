package com.vincu.core_services.complaint.repository;

import com.vincu.core_services.complaint.domain.Complaint;
import com.vincu.core_services.complaint.domain.ComplaintStatus;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@NullMarked
@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, UUID> {

    // reclamos por usuario
    @EntityGraph(attributePaths = "user")
    Page<Complaint> findByUserId(UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = "user")
    Page<Complaint> findByUserIdAndStatus(UUID userId, ComplaintStatus status, Pageable pageable);

    //filtro por estado
    @EntityGraph(attributePaths = "user")
    Page<Complaint> findByStatus(ComplaintStatus status, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "user")
    Page<Complaint> findAll(Pageable pageable);
}
