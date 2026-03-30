package com.example.weather.weather;

import com.example.weather.weather.dto.ForecastDTO;
import com.example.weather.weather.dto.WeatherDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class OpenWeatherController {

    private final WeatherService weatherService;

    @GetMapping
    public WeatherDTO getWeather(@RequestParam Long cityId) throws OpenWeatherApiClient.WeatherRetrievalException {
        return weatherService.getWeatherForLocalization(cityId);
    }

    @GetMapping("/forecast")
    public List<ForecastDTO> getForecasts(@RequestParam Long cityId) throws OpenWeatherApiClient.WeatherRetrievalException {
        return weatherService.getForecastsForLocalization(cityId);
    }

}