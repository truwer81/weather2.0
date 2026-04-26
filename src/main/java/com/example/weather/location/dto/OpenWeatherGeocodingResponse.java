package com.example.weather.location.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenWeatherGeocodingResponse(
        String name,
        String state,
        String country,
        Double lat,
        Double lon
) {
}
