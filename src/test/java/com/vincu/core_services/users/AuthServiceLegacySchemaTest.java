package com.vincu.core_services.users;

import com.vincu.core_services.core.security.JwtConfig;
import com.vincu.core_services.users.dto.LoginRequest;
import com.vincu.core_services.users.repository.UserRepository;
import com.vincu.core_services.users.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
}, showSql = false)
@Import(JwtConfig.class)
class AuthServiceLegacySchemaTest {
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UserRepository users;
    @Autowired private JwtEncoder jwtEncoder;
    @Autowired private JwtDecoder jwtDecoder;

    @Test
    void logsInWithoutLoadingProfileTables() {
        jdbc.execute("""
                create table users (
                    id uuid primary key,
                    email varchar(255) not null,
                    password varchar(255) not null,
                    role varchar(50) not null,
                    is_active boolean,
                    created_at timestamp,
                    updated_at timestamp
                )
                """);
        UUID id = UUID.randomUUID();
        var passwords = new BCryptPasswordEncoder(4);
        jdbc.update("insert into users (id, email, password, role, is_active) values (?, ?, ?, 'CLIENT', true)",
                id, "cliente@example.com", passwords.encode("test-password"));
        var auth = new AuthService(users, passwords, jwtEncoder);

        var login = auth.login(new LoginRequest("cliente@example.com", "test-password"));

        assertThat(login.id()).isEqualTo(id.toString());
        assertThat(login.email()).isEqualTo("cliente@example.com");
        assertThat(login.role()).isEqualTo("CLIENT");
        assertThat(jwtDecoder.decode(login.token()).getSubject()).isEqualTo(id.toString());
        assertThatThrownBy(() -> auth.login(new LoginRequest("cliente@example.com", "wrong")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Correo o contraseña incorrectos.");
    }
}
