package com.example.weather.localization.dto;

import com.example.weather.localization.Localization;

public record LocalizationDTO(
        Long id,
        String city,
        String country,
        String region,
        Double longitude,
        Double latitude,
        Long sortOrder
) {
    public static LocalizationDTO from(Localization localization) {
        return new LocalizationDTO(
                localization.getId(),
                localization.getCity(),
                localization.getCountry(),
                localization.getRegion(),
                localization.getLongitude(),
                localization.getLatitude(),
                localization.getSortOrder()
        );
    }
}