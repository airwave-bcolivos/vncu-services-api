package com.vincu.core_services.ubigeo.repository;

import com.vincu.core_services.ubigeo.domain.UbigeoDistrict;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@NullMarked
public interface UbigeoDistrictRepository extends JpaRepository<UbigeoDistrict, String> {

    @EntityGraph(attributePaths = {"province.department"})
    @Override
    Optional<UbigeoDistrict> findById(String id);

    List<UbigeoDistrict> findByProvinceIdOrderByNameAsc(String provinceId);
}
