package com.example.weather.auth;

import com.example.weather.location.Location;
import com.example.weather.location.LocationMapperImpl;
import com.example.weather.location.LocationService;
import com.example.weather.location.MyLocationsController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MyLocationsController.class)
@Import({SecurityConfig.class, LocationMapperImpl.class})
class MyLocationsControllerSecurityTest {

    private static final String CREATE_PAYLOAD = """
            {
              "name": "Berlin",
              "country": "Germany",
              "region": "Berlin",
              "longitude": 13.4050,
              "latitude": 52.5200
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LocationService locationService;

    @MockBean
    private AppUserRepository appUserRepository;

    @MockBean
    private DatabaseUserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "owner", roles = "USER")
    void authenticatedUser_canGetMyLocations() throws Exception {
        when(appUserRepository.findByUsername("owner")).thenReturn(Optional.of(buildUser(10L, "owner")));
        when(locationService.getPrivateLocations(10L))
                .thenReturn(List.of(new Location(1L, "Berlin", "Germany", "Berlin", 13.4050, 52.5200, 1L, buildUser(10L, "owner"))));

        mockMvc.perform(get("/api/my/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Berlin"));
    }

    @Test
    void anonymousUser_cannotGetMyLocations() throws Exception {
        mockMvc.perform(get("/api/my/locations"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "http://localhost/login"));
    }

    @Test
    @WithMockUser(username = "owner", roles = "USER")
    void authenticatedUser_canPostMyLocation() throws Exception {
        AppUser owner = buildUser(10L, "owner");
        when(appUserRepository.findByUsername("owner")).thenReturn(Optional.of(owner));
        when(locationService.createPrivateLocation(
                anyLong(),
                anyString(),
                anyDouble(),
                anyDouble(),
                anyString(),
                anyString()
        )).thenReturn(new Location(1L, "Berlin", "Germany", "Berlin", 13.4050, 52.5200, 1L, owner));

        mockMvc.perform(post("/api/my/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_PAYLOAD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Berlin"));
    }

    private AppUser buildUser(Long id, String username) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername(username);
        user.setPasswordHash("hash");
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}
