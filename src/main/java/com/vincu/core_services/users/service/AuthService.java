package com.vincu.core_services.users.service;

import lombok.extern.log4j.Log4j2;

import com.vincu.core_services.users.domain.User;
import com.vincu.core_services.users.dto.LoginRequest;
import com.vincu.core_services.users.dto.LoginResponse;
import com.vincu.core_services.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthService {

    public static final String TEMPORAL_TOKEN_VNCU = "temporal-token-vncu-123456";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.email());

        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Correo o contraseña incorrectos.");
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Correo o contraseña incorrectos.");
        }

        return new LoginResponse(
                user.getId().toString(),
                user.getEmail(),
                user.getRole(),
                TEMPORAL_TOKEN_VNCU
        );
    }
}
