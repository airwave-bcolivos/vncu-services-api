package com.vincu.core_services.ubigeo.controller;

import com.vincu.core_services.ubigeo.dto.DistrictResponse;
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

    @GetMapping("/districts")
    public ResponseEntity<List<DistrictResponse>> getDistricts(
            @RequestParam(required = false) String provinceId) {
        return ResponseEntity.ok(ubigeoService.getDistricts(provinceId));
    }
}