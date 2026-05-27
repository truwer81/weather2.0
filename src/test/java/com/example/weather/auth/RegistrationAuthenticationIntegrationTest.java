package com.example.weather.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:registration-auth;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "weather.api.openweather.key=test"
})
@AutoConfigureMockMvc
class RegistrationAuthenticationIntegrationTest {

    private static final String SHARED_LOCATION_PAYLOAD = """
            {
              "name": "Warsaw",
              "country": "Poland",
              "region": "Mazowieckie",
              "longitude": 21.0122,
              "latitude": 52.2297
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registeredUserCanLoginAndDoesNotHaveAdminAccess() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "Weather_User")
                        .param("password", "secret78")
                        .param("confirmPassword", "secret78"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        AppUser savedUser = appUserRepository.findByUsername("weather_user").orElseThrow();
        assertThat(savedUser.isEnabled()).isTrue();
        assertThat(savedUser.getRoles()).containsExactly("ROLE_USER");
        assertThat(savedUser.getPasswordHash()).isNotEqualTo("secret78");
        assertThat(passwordEncoder.matches("secret78", savedUser.getPasswordHash())).isTrue();

        MvcResult loginResult = mockMvc.perform(formLogin("/login")
                        .user("weather_user")
                        .password("secret78"))
                .andExpect(authenticated().withUsername("weather_user"))
                .andExpect(redirectedUrl("/"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.username").value("weather_user"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));

        mockMvc.perform(post("/api/locations")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SHARED_LOCATION_PAYLOAD))
                .andExpect(status().isForbidden());
    }
}
