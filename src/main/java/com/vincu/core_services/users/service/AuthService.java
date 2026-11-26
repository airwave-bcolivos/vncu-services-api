package com.vincu.core_services.users.service;

import com.vincu.core_services.core.security.JwtConfig;
import com.vincu.core_services.users.dto.LoginRequest;
import com.vincu.core_services.users.dto.LoginResponse;
import com.vincu.core_services.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    public LoginResponse login(LoginRequest request) {
        var user = userRepository.findLoginCredentialsByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Correo o contraseña incorrectos."));
        if (Boolean.FALSE.equals(user.getActive()) || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Correo o contraseña incorrectos.");
        }

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(JwtConfig.ISSUER)
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofHours(8)))
                .claim("role", user.getRole())
                .build();
        var parameters = JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims);
        return new LoginResponse(
                user.getId().toString(),
                user.getEmail(),
                user.getRole(),
                jwtEncoder.encode(parameters).getTokenValue()
        );
    }
}
