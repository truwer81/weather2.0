package com.example.weather.auth;

import com.example.weather.common.LocationNotFoundException;
import com.example.weather.weather.MyLocationWeatherController;
import com.example.weather.weather.OpenWeatherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MyLocationWeatherController.class)
@Import(SecurityConfig.class)
class MyLocationWeatherControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OpenWeatherService openWeatherService;

    @MockBean
    private AppUserRepository appUserRepository;

    @MockBean
    private DatabaseUserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "owner", roles = "USER")
    void authenticatedUser_canGetPrivateWeather() throws Exception {
        when(appUserRepository.findByUsername("owner")).thenReturn(Optional.of(TestUsers.buildUser(10L, "owner")));
        when(openWeatherService.getWeatherForOwnedLocation(10L, 24L)).thenReturn(
                new com.example.weather.weather.dto.WeatherDTO(
                        24L,
                        "Berlin",
                        "Germany",
                        "Berlin",
                        52.5200,
                        13.4050,
                        LocalDateTime.now(),
                        "clear sky",
                        21.5,
                        20.0,
                        60,
                        1012.0,
                        5.0,
                        180.0,
                        10.0
                )
        );

        mockMvc.perform(get("/api/my/locations/24/weather"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locationId").value(24))
                .andExpect(jsonPath("$.name").value("Berlin"));
    }

    @Test
    @WithMockUser(username = "owner", roles = "USER")
    void authenticatedUser_canGetPrivateForecast() throws Exception {
        when(appUserRepository.findByUsername("owner")).thenReturn(Optional.of(TestUsers.buildUser(10L, "owner")));
        when(openWeatherService.getForecastsForOwnedLocation(10L, 24L)).thenReturn(
                List.of(new com.example.weather.weather.dto.ForecastDTO(
                        LocalDateTime.of(2026, 4, 26, 15, 0),
                        18.0,
                        17.0,
                        1010.0,
                        70.0,
                        5.5,
                        200.0,
                        0.0,
                        0.0,
                        "few clouds",
                        0.2,
                        20.0
                ))
        );

        mockMvc.perform(get("/api/my/locations/24/forecast"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("few clouds"));
    }

    @Test
    void anonymousUser_cannotGetPrivateWeather() throws Exception {
        mockMvc.perform(get("/api/my/locations/24/weather"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "http://localhost/login"));
    }

    @Test
    @WithMockUser(username = "owner", roles = "USER")
    void ownedWeather_returns404WhenLocationIsMissingOrForeign() throws Exception {
        when(appUserRepository.findByUsername("owner")).thenReturn(Optional.of(TestUsers.buildUser(10L, "owner")));
        when(openWeatherService.getWeatherForOwnedLocation(10L, 99L))
                .thenThrow(new LocationNotFoundException(99L));

        mockMvc.perform(get("/api/my/locations/99/weather"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Location with id 99 not found"));
    }

    private static final class TestUsers {
        private static AppUser buildUser(Long id, String username) {
            AppUser user = new AppUser();
            user.setId(id);
            user.setUsername(username);
            user.setPasswordHash("hash");
            user.setEnabled(true);
            user.setCreatedAt(LocalDateTime.now());
            return user;
        }
    }
}
