package com.example.weather.weather.dto;

import java.time.LocalDateTime;

public record ForecastDTO(
        LocalDateTime dateTime,
        Double temperature,
        Double feelsLike,
        Double pressure,
        Double humidity,
        Double windSpeed,
        Double windDirection,
        Double rainVolume,
        Double snowVolume,
        String description,
        Double precipitationProbability,
        Double cloudsAll
) {}
