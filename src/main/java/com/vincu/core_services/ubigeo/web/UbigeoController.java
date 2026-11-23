package com.vincu.core_services.ubigeo.web;

import com.vincu.core_services.ubigeo.dto.DepartmentResponse;
import com.vincu.core_services.ubigeo.dto.DistrictResponse;
import com.vincu.core_services.ubigeo.dto.ProvinceResponse;
import com.vincu.core_services.ubigeo.service.UbigeoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ubigeo")
public class UbigeoController {

    private final UbigeoService ubigeoService;

    public UbigeoController(UbigeoService ubigeoService) {
        this.ubigeoService = ubigeoService;
    }

    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentResponse>> getDepartments() {
        return ResponseEntity.ok(ubigeoService.getDepartments());
    }

    @GetMapping("/provinces")
    public ResponseEntity<List<ProvinceResponse>> getProvinces(
            @RequestParam(required = false) String departmentId) {
        return ResponseEntity.ok(ubigeoService.getProvinces(departmentId));
    }

    @GetMapping("/districts")
    public ResponseEntity<List<DistrictResponse>> getDistricts(
            @RequestParam(required = false) String provinceId) {
        return ResponseEntity.ok(ubigeoService.getDistricts(provinceId));
    }
}