package com.example.weather.localization;

import com.example.weather.common.OpenWeatherClientException;
import com.example.weather.localization.dto.OpenWeatherGeocodingResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class OpenWeatherGeocodingClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String geoUrl;
    private final String apiKey;

    public OpenWeatherGeocodingClient(
            ObjectMapper objectMapper,
            @Value("${weather.api.openweather.geo-url}") String geoUrl,
            @Value("${weather.api.openweather.key}") String apiKey
    ) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
        this.geoUrl = geoUrl;
        this.apiKey = apiKey;
    }

    public List<OpenWeatherGeocodingResponse> search(String query, int limit) throws OpenWeatherClientException {
        try {
            String url = buildGeocodingUrl(query, limit);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new OpenWeatherClientException(
                        "Geocoding API returned status " + response.statusCode() + ": " + response.body()
                );
            }

            OpenWeatherGeocodingResponse[] rawResults =
                    objectMapper.readValue(response.body(), OpenWeatherGeocodingResponse[].class);

            return rawResults == null ? List.of() : List.of(rawResults);

        } catch (IOException e) {
            throw new OpenWeatherClientException("Failed to read geocoding API response", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OpenWeatherClientException("Geocoding API call interrupted", e);
        }
    }

    private String buildGeocodingUrl(String query, int limit) {
        return geoUrl
                + "?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8)
                + "&limit=" + limit
                + "&appid=" + apiKey;
    }
}