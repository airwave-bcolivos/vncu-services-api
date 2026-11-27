package com.vincu.core_services.ubigeo.repository;

import com.vincu.core_services.ubigeo.domain.UbigeoProvince;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UbigeoProvinceRepository extends JpaRepository<UbigeoProvince, String> {
    List<UbigeoProvince> findByDepartmentIdOrderByNameAsc(String departmentId);
}