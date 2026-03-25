package com.example.weather.weather;

import com.example.weather.weather.dto.WeatherDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping
    public WeatherDTO getWeather(@RequestParam Long cityId) throws WeatherApiClient.WeatherRetrievalException {
        return weatherService.getWeatherForLocalization(cityId);
    }
}