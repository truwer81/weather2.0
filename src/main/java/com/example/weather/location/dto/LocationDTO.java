package com.example.weather.location.dto;

public record LocationDTO(
        Long id,
        String name,
        String country,
        String region,
        Double longitude,
        Double latitude,
        Long sortOrder
) {
}
