package com.example.weather.weather;

import com.example.weather.localization.Localization;
import com.example.weather.weather.dto.OpenWeatherResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
@Slf4j
public class WeatherApiClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String baseUrl;

    public WeatherApiClient(
            ObjectMapper objectMapper,
            @Value("${weather.api.openweather.base-url}") String baseUrl,
            @Value("${weather.api.openweather.key}") String apiKey) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    public OpenWeatherResponseDTO getCurrentWeather(Localization localization) throws WeatherRetrievalException {
        try {
            String url = buildUrl(localization);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new WeatherRetrievalException(
                        "Weather API returned status " + response.statusCode() + ": " + response.body()
                );
            }

            return objectMapper.readValue(response.body(), OpenWeatherResponseDTO.class);

        } catch (IOException e) {
            throw new WeatherRetrievalException("Failed to read weather API response", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WeatherRetrievalException("Weather API call interrupted", e);
        }
    }

    private String buildUrl(Localization localization) {
        return baseUrl
                + "?lat=" + localization.getLatitude()
                + "&lon=" + localization.getLongitude()
                + "&appid=" + apiKey
                + "&units=metric";
    }

    public static class WeatherRetrievalException extends Exception {
        public WeatherRetrievalException(String message) {
            super(message);
        }

        public WeatherRetrievalException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}