package com.vincu.core_services.ubigeo.service;

import com.vincu.core_services.ubigeo.dto.DistrictResponse;
import com.vincu.core_services.ubigeo.repository.UbigeoDistrictRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UbigeoService {

    private final UbigeoDistrictRepository districtRepository;

    public UbigeoService(UbigeoDistrictRepository districtRepository) {
        this.districtRepository = districtRepository;
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