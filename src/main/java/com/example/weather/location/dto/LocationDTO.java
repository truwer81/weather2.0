package com.example.weather.location.dto;

import com.example.weather.location.Location;

public record LocationDTO(
        Long id,
        String name,
        String country,
        String region,
        Double longitude,
        Double latitude,
        Long sortOrder
) {
    public static LocationDTO from(Location location) {
        return new LocationDTO(
                location.getId(),
                location.getName(),
                location.getCountry(),
                location.getRegion(),
                location.getLongitude(),
                location.getLatitude(),
                location.getSortOrder()
        );
    }
}