package com.example.weather.location.dto;

public record LocationSearchResultDTO(
        String label,
        String name,
        String region,
        String country,
        Double latitude,
        Double longitude
) {
}
