package com.vincu.core_services.users.web;

import com.vincu.core_services.users.dto.ClientRegisterRequest;
import com.vincu.core_services.users.dto.TechnicianRegisterRequest;
import com.vincu.core_services.users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register/client")
    public ResponseEntity<Void> registerClient(@Valid @RequestBody ClientRegisterRequest request) {
        userService.registerClient(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/register/technician")
    public ResponseEntity<Void> registerTechnician(@Valid @RequestBody TechnicianRegisterRequest request) {
        userService.registerTechnician(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
