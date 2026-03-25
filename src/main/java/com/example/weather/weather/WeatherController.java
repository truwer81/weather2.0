package com.example.weather.weather;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WeatherController {

    private final ObjectMapper objectMapper;
    private final WeatherService weatherService;

    public WeatherController(ObjectMapper objectMapper, WeatherService weatherService) {
        this.objectMapper = objectMapper;
        this.weatherService = weatherService;
    }

    // TODO: add forecast support with dedicated OpenWeather forecast endpoint

// GET /weather?localization={localizationId}
    public String getCurrentWeather(Long localizationId) throws JsonProcessingException {
        try {
            var weather = weatherService.getCurrentWeather(localizationId);
            return objectMapper.writeValueAsString(weather);
        } catch (WeatherAPIClient.WeatherRetrievalException e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        } catch (Exception e) {
            return "{\"error\": \"Internal server error\"}";
        }
    }
}
