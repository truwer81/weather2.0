package com.example.weather.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AuthPageController.class)
@Import(SecurityConfig.class)
class RegistrationPageSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegistrationService registrationService;

    @MockBean
    private DatabaseUserDetailsService userDetailsService;

    @Test
    void registerPage_isPublic() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    void validRegistrationForm_redirectsToLoginRegistered() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "new_user")
                        .param("password", "secret78")
                        .param("confirmPassword", "secret78"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        verify(registrationService).register(any());
    }

    @Test
    void duplicateUsername_returnsRegisterFormWithUsernameError() throws Exception {
        doThrow(new RegistrationService.RegistrationException("username", "Username is already taken."))
                .when(registrationService)
                .register(any());

        mockMvc.perform(post("/register")
                        .param("username", "new_user")
                        .param("password", "secret78")
                        .param("confirmPassword", "secret78"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("registerRequest", "username"));
    }

    @Test
    void shortPassword_returnsRegisterFormBeforeCallingService() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "new_user")
                        .param("password", "short")
                        .param("confirmPassword", "short"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("registerRequest", "password"));

        verify(registrationService, never()).register(any());
    }
}
