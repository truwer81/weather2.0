package com.example.weather.auth;

import com.example.weather.auth.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    private PasswordEncoder passwordEncoder;
    private RegistrationService registrationService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        registrationService = new RegistrationService(appUserRepository, passwordEncoder);
    }

    @Test
    void register_createsEnabledUserWithRoleUserAndHashedPassword() {
        when(appUserRepository.existsByUsername("new_user")).thenReturn(false);
        when(appUserRepository.saveAndFlush(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        registrationService.register(request(" New_User ", "secret78", "secret78"));

        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).saveAndFlush(userCaptor.capture());

        AppUser savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo("new_user");
        assertThat(savedUser.isEnabled()).isTrue();
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getRoles()).containsExactly("ROLE_USER");
        assertThat(savedUser.getPasswordHash()).isNotEqualTo("secret78");
        assertThat(passwordEncoder.matches("secret78", savedUser.getPasswordHash())).isTrue();
    }

    @Test
    void register_rejectsDuplicateUsernameBeforeSave() {
        when(appUserRepository.existsByUsername("new_user")).thenReturn(true);

        assertThatThrownBy(() -> registrationService.register(request("new_user", "secret78", "secret78")))
                .isInstanceOf(RegistrationService.RegistrationException.class)
                .hasMessage("Username is already taken.")
                .extracting("field")
                .isEqualTo("username");

        verify(appUserRepository, never()).saveAndFlush(any(AppUser.class));
    }

    @Test
    void register_rejectsShortPassword() {
        assertThatThrownBy(() -> registrationService.register(request("new_user", "short", "short")))
                .isInstanceOf(RegistrationService.RegistrationException.class)
                .hasMessage("Password must be at least 8 characters long.")
                .extracting("field")
                .isEqualTo("password");

        verify(appUserRepository, never()).saveAndFlush(any(AppUser.class));
    }

    @Test
    void register_rejectsPasswordMismatch() {
        assertThatThrownBy(() -> registrationService.register(request("new_user", "secret78", "different8")))
                .isInstanceOf(RegistrationService.RegistrationException.class)
                .hasMessage("Passwords do not match.")
                .extracting("field")
                .isEqualTo("confirmPassword");

        verify(appUserRepository, never()).saveAndFlush(any(AppUser.class));
    }

    @Test
    void register_mapsSaveRaceConditionToUsernameError() {
        when(appUserRepository.existsByUsername("new_user")).thenReturn(false);
        when(appUserRepository.saveAndFlush(any(AppUser.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> registrationService.register(request("new_user", "secret78", "secret78")))
                .isInstanceOf(RegistrationService.RegistrationException.class)
                .hasMessage("Username is already taken.")
                .extracting("field")
                .isEqualTo("username");
    }

    private RegisterRequest request(String username, String password, String confirmPassword) {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setPassword(password);
        request.setConfirmPassword(confirmPassword);
        return request;
    }
}
