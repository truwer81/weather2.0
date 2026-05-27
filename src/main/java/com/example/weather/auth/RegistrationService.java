package com.example.weather.auth;

import com.example.weather.auth.dto.RegisterRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-z0-9_-]{3,30}$");
    private static final String USER_ROLE = "ROLE_USER";

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(RegisterRequest request) {
        String username = normalizeUsername(request.getUsername());

        validateUsername(username);
        validatePassword(request.getPassword(), request.getConfirmPassword());

        if (appUserRepository.existsByUsername(username)) {
            throw new RegistrationException("username", "Username is already taken.");
        }

        AppUser user = AppUser.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .roles(Set.of(USER_ROLE))
                .build();

        try {
            appUserRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            throw new RegistrationException("username", "Username is already taken.", ex);
        }
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    private void validateUsername(String username) {
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new RegistrationException(
                    "username",
                    "Username must be 3-30 characters and use only lowercase letters, numbers, underscore or hyphen."
            );
        }
    }

    private void validatePassword(String password, String confirmPassword) {
        if (password == null || password.isBlank() || password.length() < 8) {
            throw new RegistrationException("password", "Password must be at least 8 characters long.");
        }

        if (confirmPassword == null || !password.equals(confirmPassword)) {
            throw new RegistrationException("confirmPassword", "Passwords do not match.");
        }
    }

    @Getter
    public static class RegistrationException extends RuntimeException {
        private final String field;

        public RegistrationException(String field, String message) {
            super(message);
            this.field = field;
        }

        public RegistrationException(String field, String message, Throwable cause) {
            super(message, cause);
            this.field = field;
        }
    }
}
