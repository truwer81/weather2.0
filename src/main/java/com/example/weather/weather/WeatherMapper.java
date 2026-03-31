package com.example.weather.weather;

import com.example.weather.localization.Localization;
import com.example.weather.weather.dto.WeatherDTO;
import com.example.weather.weather.dto.WeatherResponseDTO;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class WeatherMapper {

    private WeatherMapper() {
    }


    public static Weather toEntity(Localization localization, WeatherResponseDTO response) {
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

    private static String extractDescription(WeatherResponseDTO response) {
        if (response.getWeather() == null || response.getWeather().isEmpty()) {
            return null;
        }
        return response.getWeather().getFirst().getDescription();
    }

    private static Double extractTemperature(WeatherResponseDTO response) {
        return response.getMainInfo() != null ? response.getMainInfo().getTemp() : null;
    }

    private static Double extractFeelsLike(WeatherResponseDTO response) {
        return response.getMainInfo() != null ? response.getMainInfo().getFeelsLike() : null;
    }

    private static Double extractPressure(WeatherResponseDTO response) {
        return response.getMainInfo() != null ? response.getMainInfo().getPressure() : null;
    }

    private static Integer extractHumidity(WeatherResponseDTO response) {
        return response.getMainInfo() != null && response.getMainInfo().getHumidity() != null
                ? response.getMainInfo().getHumidity().intValue()
                : null;
    }

    private static Double extractWindSpeed(WeatherResponseDTO response) {
        return response.getWindInfo() != null ? response.getWindInfo().getWindSpeed() : null;
    }

    private static Double extractWindDeg(WeatherResponseDTO response) {
        return response.getWindInfo() != null ? response.getWindInfo().getWindDeg() : null;
    }

    private static Double extractCloudsAll(WeatherResponseDTO response) {
        return response.getCloudsInfo() != null ? response.getCloudsInfo().getCloudsAll() : null;
    }

    private static LocalDateTime extractProviderTimestamp(WeatherResponseDTO response) {
        if (response.getForecastTimestamp() == null) {
            return null;
        }

        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(response.getForecastTimestamp()),
                ZoneOffset.UTC
        );
    }
}