package com.example.weather.weather;

import com.example.weather.localization.Localization;
import com.example.weather.weather.dto.OpenWeatherResponseDTO;
import com.example.weather.weather.dto.WeatherDTO;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class WeatherMapper {

    private WeatherMapper() {
    }

    public static Weather toEntity(Localization localization, OpenWeatherResponseDTO response) {
        Weather weather = new Weather();

        weather.setLocalization(localization);
        weather.setDescription(extractDescription(response));
        weather.setTemperature(extractTemperature(response));
        weather.setFeelsLike(extractFeelsLike(response));
        weather.setPressure(extractPressure(response));
        weather.setHumidity(extractHumidity(response));
        weather.setWindSpeed(extractWindSpeed(response));
        weather.setWindDeg(extractWindDeg(response));
        weather.setCloudsAll(extractCloudsAll(response));
        weather.setProviderTimestamp(extractProviderTimestamp(response));
        weather.setFetchedAt(LocalDateTime.now());

        return weather;
    }

    public static WeatherDTO toDTO(Weather weather) {
        return new WeatherDTO(
                weather.getLocalization().getId(),
                weather.getLocalization().getCity(),
                weather.getLocalization().getCountry(),
                weather.getLocalization().getRegion(),
                weather.getLocalization().getLatitude(),
                weather.getLocalization().getLongitude(),
                weather.getProviderTimestamp(),
                weather.getDescription(),
                weather.getTemperature(),
                weather.getFeelsLike(),
                weather.getHumidity(),
                weather.getPressure(),
                weather.getWindSpeed(),
                weather.getWindDeg(),
                weather.getCloudsAll()
        );
    }

    private static String extractDescription(OpenWeatherResponseDTO response) {
        if (response.getWeather() == null || response.getWeather().isEmpty()) {
            return null;
        }
        return response.getWeather().get(0).getDescription();
    }

    private static Double extractTemperature(OpenWeatherResponseDTO response) {
        return response.getMainInfo() != null ? response.getMainInfo().getTemp() : null;
    }

    private static Double extractFeelsLike(OpenWeatherResponseDTO response) {
        return response.getMainInfo() != null ? response.getMainInfo().getFeelsLike() : null;
    }

    private static Double extractPressure(OpenWeatherResponseDTO response) {
        return response.getMainInfo() != null ? response.getMainInfo().getPressure() : null;
    }

    private static Integer extractHumidity(OpenWeatherResponseDTO response) {
        return response.getMainInfo() != null && response.getMainInfo().getHumidity() != null
                ? response.getMainInfo().getHumidity().intValue()
                : null;
    }

    private static Double extractWindSpeed(OpenWeatherResponseDTO response) {
        return response.getWindInfo() != null ? response.getWindInfo().getWindSpeed() : null;
    }

    private static Double extractWindDeg(OpenWeatherResponseDTO response) {
        return response.getWindInfo() != null ? response.getWindInfo().getWindDeg() : null;
    }

    private static Double extractCloudsAll(OpenWeatherResponseDTO response) {
        return response.getCloudsInfo() != null ? response.getCloudsInfo().getCloudsAll() : null;
    }

    private static LocalDateTime extractProviderTimestamp(OpenWeatherResponseDTO response) {
        if (response.getForecastTimestamp() == null) {
            return null;
        }

        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(response.getForecastTimestamp()),
                ZoneOffset.UTC
        );
    }
}