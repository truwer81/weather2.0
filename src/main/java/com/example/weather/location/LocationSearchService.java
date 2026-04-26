package com.example.weather.location;

import com.example.weather.common.BadRequestException;
import com.example.weather.common.ExternalServiceException;
import com.example.weather.common.OpenWeatherClientException;
import com.example.weather.location.dto.LocationSearchResultDTO;
import com.example.weather.location.dto.OpenWeatherGeocodingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LocationSearchService {

    private static final int DEFAULT_LIMIT = 5;

    private final OpenWeatherGeocodingClient openWeatherGeocodingClient;

    public List<LocationSearchResultDTO> search(String query) {
        String normalizedQuery = normalizeQuery(query);

        try {
            return deduplicate(openWeatherGeocodingClient.search(normalizedQuery, DEFAULT_LIMIT)).stream()
                    .map(this::mapToResultDto)
                    .toList();
        } catch (OpenWeatherClientException e) {
            throw new ExternalServiceException("Could not fetch location suggestions from OpenWeather", e);
        }
    }

    private String normalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            throw new BadRequestException("Search query must not be blank");
        }

        String normalized = query.trim();

        if (normalized.length() > 120) {
            throw new BadRequestException("Search query is too long");
        }

        return normalized;
    }

    private List<OpenWeatherGeocodingResponse> deduplicate(List<OpenWeatherGeocodingResponse> input) {
        Map<String, OpenWeatherGeocodingResponse> unique = new LinkedHashMap<>();

        for (OpenWeatherGeocodingResponse item : input) {
            String key = buildDeduplicationKey(item);
            unique.putIfAbsent(key, item);
        }

        return unique.values().stream().toList();
    }

    private String buildDeduplicationKey(OpenWeatherGeocodingResponse item) {
        return normalizeText(item.name()) + "|"
                + normalizeText(item.state()) + "|"
                + normalizeText(item.country()) + "|"
                + roundCoordinate(item.lat()) + "|"
                + roundCoordinate(item.lon());
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private String roundCoordinate(Double value) {
        if (value == null) {
            return "";
        }
        return String.format("%.4f", value);
    }

    private LocationSearchResultDTO mapToResultDto(OpenWeatherGeocodingResponse item) {
        return new LocationSearchResultDTO(
                buildLabel(item),
                safe(item.name()),
                safe(item.state()),
                safe(item.country()),
                item.lat(),
                item.lon()
        );
    }

    private String buildLabel(OpenWeatherGeocodingResponse item) {
        String city = safe(item.name());
        String region = safe(item.state());
        String country = safe(item.country());

        if (!region.isBlank()) {
            return city + ", " + region + ", " + country;
        }

        return city + ", " + country;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}