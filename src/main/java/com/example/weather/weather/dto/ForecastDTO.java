package com.example.weather.weather.dto;

import java.time.LocalDateTime;

public record ForecastDTO(
        LocalDateTime dateTime,
        double temperature,
        double feelsLike,
        double pressure,
        double humidity,
        double windSpeed,
        double windDirection,
        double rainVolume,
        double snowVolume,
        String description,
        double precipitationProbability,
        double cloudsAll
) {}
