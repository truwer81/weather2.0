package com.example.weather.weather;

import com.example.weather.auth.AppUserRepository;
import com.example.weather.weather.dto.ForecastDTO;
import com.example.weather.weather.dto.WeatherDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/my/locations/{id}")
@RequiredArgsConstructor
public class MyLocationWeatherController {

    private final OpenWeatherService openWeatherService;
    private final AppUserRepository appUserRepository;

    @GetMapping("/weather")
    public WeatherDTO getMyLocationWeather(Authentication authentication, @PathVariable("id") Long locationId) {
        var ownerId = getCurrentUserId(authentication);
        return openWeatherService.getWeatherForOwnedLocation(ownerId, locationId);
    }

    @GetMapping("/forecast")
    public List<ForecastDTO> getMyLocationForecast(Authentication authentication, @PathVariable("id") Long locationId) {
        var ownerId = getCurrentUserId(authentication);
        return openWeatherService.getForecastsForOwnedLocation(ownerId, locationId);
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found");
        }

        var user = appUserRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));

        return user.getId();
    }
}
