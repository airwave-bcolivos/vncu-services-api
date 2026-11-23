package com.vincu.core_services.ubigeo.service;

import com.vincu.core_services.ubigeo.dto.DepartmentResponse;
import com.vincu.core_services.ubigeo.dto.DistrictResponse;
import com.vincu.core_services.ubigeo.dto.ProvinceResponse;
import com.vincu.core_services.ubigeo.repository.UbigeoDepartmentRepository;
import com.vincu.core_services.ubigeo.repository.UbigeoDistrictRepository;
import com.vincu.core_services.ubigeo.repository.UbigeoProvinceRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UbigeoService {

    private final UbigeoDepartmentRepository departmentRepository;
    private final UbigeoProvinceRepository provinceRepository;
    private final UbigeoDistrictRepository districtRepository;

    public UbigeoService(UbigeoDepartmentRepository departmentRepository,
                         UbigeoProvinceRepository provinceRepository,
                         UbigeoDistrictRepository districtRepository) {
        this.departmentRepository = departmentRepository;
        this.provinceRepository = provinceRepository;
        this.districtRepository = districtRepository;
    }
    public List<DepartmentResponse> getDepartments() {
        return departmentRepository.findAllByOrderByNameAsc().stream()
                .map(d -> new DepartmentResponse(d.getId(), d.getName()))
                .toList();
    }

    public List<ProvinceResponse> getProvinces(String departmentId) {
        var provinces = (departmentId == null || departmentId.isBlank())
                ? provinceRepository.findAll(Sort.by("name"))
                : provinceRepository.findByDepartmentIdOrderByNameAsc(departmentId.trim());

        return provinces.stream()
                .map(p -> new ProvinceResponse(p.getId(), p.getName(), p.getDepartmentId()))
                .toList();
    }

    public List<DistrictResponse> getDistricts(String provinceId) {
        var districts = (provinceId == null || provinceId.isBlank())
                ? districtRepository.findAll(Sort.by("name"))
                : districtRepository.findByProvinceIdOrderByNameAsc(provinceId.trim());

        return districts.stream()
                .map(d -> new DistrictResponse(d.getId(), d.getName(), d.getProvinceId()))
                .toList();
    }
}