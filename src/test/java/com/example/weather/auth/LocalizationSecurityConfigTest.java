package com.example.weather.auth;

import com.example.weather.localization.Localization;
import com.example.weather.localization.LocalizationController;
import com.example.weather.localization.LocalizationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LocalizationController.class)
@Import(SecurityConfig.class)
class LocalizationSecurityConfigTest {

    private static final String CREATE_OR_UPDATE_PAYLOAD = """
            {
              "city": "Warsaw",
              "country": "Poland",
              "region": "Mazowieckie",
              "longitude": 21.0122,
              "latitude": 52.2297
            }
            """;

    private static final String ORDER_PAYLOAD = """
            [
              {
                "localizationId": 1,
                "sortOrder": 1
              }
            ]
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LocalizationService localizationService;

    @MockBean
    private DatabaseUserDetailsService userDetailsService;

    @Test
    void getCities_isPublic() throws Exception {
        when(localizationService.getAllLocalizations())
                .thenReturn(List.of(new Localization(1L, "Warsaw", "Poland", "Mazowieckie", 21.0122, 52.2297, 1L)));

        mockMvc.perform(get("/api/cities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].city").value("Warsaw"));
    }

    @Test
    void anonymousUser_cannotCreateCity() throws Exception {
        mockMvc.perform(post("/api/cities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_OR_UPDATE_PAYLOAD))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "http://localhost/login"));
    }

    @Test
    void anonymousUser_cannotUpdateCity() throws Exception {
        mockMvc.perform(put("/api/cities/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_OR_UPDATE_PAYLOAD))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "http://localhost/login"));
    }

    @Test
    void anonymousUser_cannotReorderCities() throws Exception {
        mockMvc.perform(put("/api/cities/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ORDER_PAYLOAD))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "http://localhost/login"));
    }

    @Test
    void anonymousUser_cannotDeleteCity() throws Exception {
        mockMvc.perform(delete("/api/cities/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "http://localhost/login"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminUser_canCreateCity() throws Exception {
        when(localizationService.createLocalization(
                anyString(),
                anyDouble(),
                anyDouble(),
                anyString(),
                anyString()
        )).thenReturn(new Localization(1L, "Warsaw", "Poland", "Mazowieckie", 21.0122, 52.2297, 1L));

        mockMvc.perform(post("/api/cities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_OR_UPDATE_PAYLOAD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Warsaw"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminUser_canUpdateCity() throws Exception {
        when(localizationService.updateLocalization(
                anyLong(),
                anyString(),
                anyDouble(),
                anyDouble(),
                anyString(),
                anyString()
        )).thenReturn(new Localization(1L, "Warsaw", "Poland", "Mazowieckie", 21.0122, 52.2297, 1L));

        mockMvc.perform(put("/api/cities/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_OR_UPDATE_PAYLOAD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Warsaw"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminUser_canReorderCities() throws Exception {
        when(localizationService.saveDisplayOrder(org.mockito.ArgumentMatchers.<List<com.example.weather.localization.dto.OrderByDTO>>any()))
                .thenReturn(List.of(new Localization(1L, "Warsaw", "Poland", "Mazowieckie", 21.0122, 52.2297, 1L)));

        mockMvc.perform(put("/api/cities/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ORDER_PAYLOAD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].city").value("Warsaw"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminUser_canDeleteCity() throws Exception {
        mockMvc.perform(delete("/api/cities/1"))
                .andExpect(status().isNoContent());
    }
}
