package com.vincu.core_services.ubigeo.repository;

import com.vincu.core_services.ubigeo.domain.UbigeoDistrict;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UbigeoDistrictRepository extends JpaRepository<UbigeoDistrict, String> {

    List<UbigeoDistrict> findByProvinceIdOrderByNameAsc(String provinceId);
}
