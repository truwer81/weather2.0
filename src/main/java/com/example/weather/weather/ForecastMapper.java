package com.example.weather.weather;

import com.example.weather.localization.Localization;
import com.example.weather.weather.dto.ForecastDTO;
import com.example.weather.weather.dto.ForecastResponseDTO;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public final class ForecastMapper {

    private ForecastMapper() {
    }

    public static List<Forecast> toEntities(Localization localization, ForecastResponseDTO response) {
        if (response == null || response.list() == null || response.list().isEmpty()) {
            return Collections.emptyList();
        }

        Integer timezoneSeconds = response.city() != null ? response.city().timezone() : null;

        return response.list().stream()
                .map(item -> toEntity(localization, item, timezoneSeconds))
                .toList();
    }

    public static Forecast toEntity(
            Localization localization,
            ForecastResponseDTO.ForecastItemDTO item,
            Integer timezoneSeconds
    ) {
        Forecast forecast = new Forecast();

        forecast.setLocalization(localization);
        forecast.setForecastTime(toTimestamp(item.dt(), timezoneSeconds));
        forecast.setTemperature(item.main() != null ? item.main().temp() : 0.0);
        forecast.setFeelsLike(item.main() != null ? item.main().feelsLike() : 0.0);
        forecast.setPressure(item.main() != null ? (double) item.main().pressure() : 0.0);
        forecast.setHumidity(item.main() != null ? item.main().humidity() : 0);
        forecast.setWindSpeed(item.wind() != null ? item.wind().speed() : 0.0);
        forecast.setWindDeg(item.wind() != null ? (double) item.wind().deg() : 0.0);
        forecast.setDescription(extractDescription(item));
        forecast.setCloudsAll(item.clouds() != null ? defaultDouble(item.clouds().all()) : 0.0);
        forecast.setPrecipitationProbability(defaultDouble(item.pop()));
        forecast.setRainVolume(item.rain() != null ? defaultDouble(item.rain().volumeLast3h()) : 0.0);
        forecast.setSnowVolume(item.snow() != null ? defaultDouble(item.snow().volumeLast3h()) : 0.0);
        forecast.setFetchedAt(LocalDateTime.now());

        return forecast;
    }


    private static Timestamp toTimestamp(long epochSeconds, Integer timezoneSeconds) {
        if (timezoneSeconds == null) {
            return Timestamp.from(Instant.ofEpochSecond(epochSeconds));
        }

        LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(
                epochSeconds,
                0,
                java.time.ZoneOffset.ofTotalSeconds(timezoneSeconds)
        );

        return Timestamp.valueOf(localDateTime);
    }

    private static String extractDescription(ForecastResponseDTO.ForecastItemDTO item) {
        if (item.weather() == null || item.weather().isEmpty()) {
            return null;
        }

        ForecastResponseDTO.WeatherDTO firstWeather = item.weather().get(0);
        return firstWeather != null ? firstWeather.description() : null;
    }

    public static List<ForecastDTO> toDTOList(List<Forecast> forecasts) {
        if (forecasts == null || forecasts.isEmpty()) {
            return Collections.emptyList();
        }

        return forecasts.stream()
                .map(ForecastMapper::toDTO)
                .toList();
    }

    public static ForecastDTO toDTO(Forecast forecast) {
        if (forecast == null) {
            return null;
        }

        return new ForecastDTO(
                toLocalDateTime(forecast.getForecastTime()),
                defaultDouble(forecast.getTemperature()),
                defaultDouble(forecast.getFeelsLike()),
                defaultDouble(forecast.getPressure()),
                defaultDouble(forecast.getHumidity()),
                defaultDouble(forecast.getWindSpeed()),
                defaultDouble(forecast.getWindDeg()),
                defaultDouble(forecast.getRainVolume()),
                defaultDouble(forecast.getSnowVolume()),
                defaultString(forecast.getDescription()),
                defaultDouble(forecast.getPrecipitationProbability()),
                defaultDouble(forecast.getCloudsAll())
        );
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    private static double defaultDouble(Number value) {
        return value != null ? value.doubleValue() : 0.0;
    }

    private static String defaultString(String value) {
        return value != null ? value : "";
    }
}