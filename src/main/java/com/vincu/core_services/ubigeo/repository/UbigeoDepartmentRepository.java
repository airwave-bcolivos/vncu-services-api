package com.vincu.core_services.ubigeo.repository;

import com.vincu.core_services.ubigeo.domain.UbigeoDepartment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UbigeoDepartmentRepository extends JpaRepository<UbigeoDepartment, String> {
    List<UbigeoDepartment> findAllByOrderByNameAsc();
}
