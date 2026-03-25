package com.example.server;

import com.example.server.localization.LocalizationController;
import com.example.server.localization.LocalizationRepository;
import com.example.server.localization.LocalizationService;
import com.example.server.weather.WeatherAPIClient;
import com.example.server.weather.WeatherController;
import com.example.server.weather.WeatherService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Server {

    private final LocalizationController localizationController;
    private final WeatherController weatherController;

    public Server() {
        var objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        var sessionFactory = HibernateUtils.getSessionFactory();
        var localizationRepository = new LocalizationRepository(sessionFactory);
        var localizationService = new LocalizationService(localizationRepository);
        var localizationController = new LocalizationController(objectMapper, localizationService);
        var httpClient = HttpClient.newHttpClient();

        WeatherAPIClient weatherAPIClient = new WeatherAPIClient(httpClient, objectMapper);
        WeatherService weatherService = new WeatherService(weatherAPIClient, localizationRepository);
        this.weatherController = new WeatherController(objectMapper, weatherService);
        this.localizationController = localizationController;
    }

    // mapujemy requesty HTTP na metody kontrolera
    public String callServer(String method, String path, String json) throws HttpRequestException {
        try {
            URI uri = new URI(path);
            if (Objects.equals(method, "POST") && path.startsWith("/localizations")) {
                return localizationController.createLocalization(json);
            } else if (Objects.equals(method, "GET") && path.startsWith("/localizations")) {
                return localizationController.getLocalizations();
            } else if (Objects.equals(method, "GET") && path.startsWith("/weather")) {
                var query = uri.getQuery();
                Map<String, String> queryParams = splitQuery(query);
                var localizationId = toInt(queryParams.get("localization"));
                return weatherController.getCurrentWeather(localizationId);
            }
        } catch (URISyntaxException e) {
            throw new HttpRequestException("Invalid URI syntax: " + path, e);
        } catch (JsonProcessingException e) {
            throw new HttpRequestException("JSON processing error: " + json, e);
        }
        return "404";
    }

    public static Map<String, String> splitQuery(String query) throws URISyntaxException {
        Map<String, String> queryPairs = new HashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            queryPairs.put(pair.substring(0, idx), pair.substring(idx + 1));
        }
        return queryPairs;
    }


    public long toInt(String nrToStr) throws IllegalArgumentException {
        try {
            return Long.parseLong(nrToStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("error converting localizationId to long: " + nrToStr, e);
        }
    }

    public class HttpRequestException extends Exception {
        public HttpRequestException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
