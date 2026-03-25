package com.example.weather.weather;

import com.example.config.AppConfig;
import com.example.weather.localization.Localization;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Component
public class WeatherAPIClient {

    private HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String baseUrl;
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";


    public WeatherAPIClient(HttpClient httpClient, ObjectMapper objectMapper, String baseUrl) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.apiKey = AppConfig.getRequired("OPENWEATHER_API_KEY");
    }

    public Weather getCurrentWeather(Localization localization) throws WeatherRetrievalException {
        try {
            URI uri = buildUri(localization.getLatitude(), localization.getLongitude());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new WeatherRetrievalException(
                        "OpenWeather returned status " + response.statusCode() + ": " + response.body()
                );
            }

            WeatherResponseDTO dto = objectMapper.readValue(response.body(), WeatherResponseDTO.class);
            return mapToWeather(dto, localization);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WeatherRetrievalException("Request interrupted while fetching weather data", e);
        } catch (IOException e) {
            throw new WeatherRetrievalException("Failed to fetch weather data", e);
        }
    }

    private URI buildUri(double latitude, double longitude) {
        String url = BASE_URL
                + "?lat=" + latitude
                + "&lon=" + longitude
                + "&appid=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8)
                + "&units=metric"
                + "&lang=pl";

        return URI.create(url);
    }

    private Weather mapToWeather(WeatherResponseDTO dto, Localization localization) {
        Weather weather = new Weather();

        if (dto.getWeather() != null && !dto.getWeather().isEmpty()) {
            weather.setDescription(dto.getWeather().get(0).getDescription());
        }

        if (dto.getMainInfo() != null) {
            weather.setTemp(dto.getMainInfo().getTemp());
            weather.setFeelsLike(dto.getMainInfo().getFeelsLike());
            weather.setPressure(dto.getMainInfo().getPressure());
            weather.setHumidity(dto.getMainInfo().getHumidity());
        }

        if (dto.getWindInfo() != null) {
            weather.setWindSpeed(dto.getWindInfo().getWindSpeed());
            weather.setWindDeg(dto.getWindInfo().getWindDeg());
        }

        if (dto.getCloudsInfo() != null) {
            weather.setCloudsAll(dto.getCloudsInfo().getCloudsAll());
        }

        weather.setForecastTimestamp(dto.getForecastTimestamp());

        return weather;
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