package com.example.weather.weather.dto;

import java.time.Instant;
import java.time.LocalDateTime;

public record ForecastPoint(
        LocalDateTime dateTime,
        double temperature,
        double feelsLike,
        double pressure,
        double humidity,
        double windSpeed,
        double windDirection,
        double rainVolume,
        double snowVolume,
        Instant fetchedAt,
        String description,
        double precipitationProbability,
        double cloudsAll
) {
}
