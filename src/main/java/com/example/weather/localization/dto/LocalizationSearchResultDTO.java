package com.example.weather.localization.dto;

public record LocalizationSearchResultDTO(
        String label,
        String city,
        String region,
        String country,
        Double latitude,
        Double longitude
) {
}
