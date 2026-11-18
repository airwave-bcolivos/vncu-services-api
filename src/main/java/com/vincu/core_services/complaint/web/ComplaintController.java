package com.vincu.core_services.complaint.web;

import com.vincu.core_services.complaint.domain.ComplaintStatus;
import com.vincu.core_services.complaint.dto.ComplaintRequest;
import com.vincu.core_services.complaint.dto.ComplaintResponse;
import com.vincu.core_services.complaint.service.ComplaintService;
import com.vincu.core_services.jobs.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @PostMapping
    public ResponseEntity<ComplaintResponse> createComplaint(@Valid @RequestBody ComplaintRequest complaint) {
        ComplaintResponse response = complaintService.createComplaint(complaint);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<ComplaintResponse>> getUserComplaints(
            @RequestParam UUID userId,
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(complaintService.getComplaintsByUser(userId, status, page, size));
    }

    @GetMapping("/all")
    public ResponseEntity<PageResponse<ComplaintResponse>> getAllComplaints(
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(complaintService.getAllComplaints(status, page, size));
    }
}
