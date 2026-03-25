package com.example.weather.weather.dto;

import java.time.LocalDateTime;

public record WeatherDTO(
        Long localizationId,
        String city,
        String country,
        String region,
        Double latitude,
        Double longitude,
        LocalDateTime providerTimestamp,
        String description,
        Double temperature,
        Double feelsLike,
        Integer humidity,
        Double pressure,
        Double windSpeed,
        Double windDeg,
        Double cloudsPercentage
) {
}