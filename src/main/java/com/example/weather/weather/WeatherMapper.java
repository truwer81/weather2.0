package com.example.weather.weather;

import com.example.weather.localization.Localization;
import com.example.weather.weather.dto.ForecastPoint;
import com.example.weather.weather.dto.ForecastResponseDTO;
import com.example.weather.weather.dto.WeatherDTO;
import com.example.weather.weather.dto.WeatherResponseDTO;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

public final class WeatherMapper {

    private WeatherMapper() {
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
        forecast.setTemperature(item.main() != null ? item.main().temp() : null);
        forecast.setFeelsLike(item.main() != null ? item.main().feelsLike() : null);
        forecast.setPressure(item.main() != null ? (double) item.main().pressure() : null);
        forecast.setHumidity(item.main() != null ? item.main().humidity() : null);
        forecast.setWindSpeed(item.wind() != null ? item.wind().speed() : null);
        forecast.setWindDeg(item.wind() != null ? (double) item.wind().deg() : null);
        forecast.setDescription(extractDescription(item));
        forecast.setCloudsAll(null); // patrz uwaga niżej
        forecast.setPrecipitationProbability(item.pop());
        forecast.setRainVolume(item.rain() != null ? item.rain().volumeLast3h() : null);
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

    public static ForecastPoint toDTO(Forecast forecast) {
        if (forecast == null) {
            return null;
        }

        return new ForecastPoint(
                toLocalDateTime(forecast.getForecastTime()),
                defaultDouble(forecast.getTemperature()),
                defaultDouble(forecast.getFeelsLike()),
                defaultDouble(forecast.getPressure()),
                defaultDouble(forecast.getHumidity()),
                defaultDouble(forecast.getWindSpeed()),
                defaultDouble(forecast.getWindDeg()),
                forecast.getRainVolume(),
                forecast.getSnowVolume(),
                toInstant(forecast.getFetchedAt()),
                forecast.getDescription(),
                defaultDouble(forecast.getPrecipitationProbability()),
                forecast.getCloudsAll()
        );
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    private static Instant toInstant(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toInstant(java.time.ZoneOffset.UTC) : null;
    }

    private static double defaultDouble(Number value) {
        return value != null ? value.doubleValue() : 0.0;
    }

    private static String extractDescription(WeatherResponseDTO response) {
        if (response.getWeather() == null || response.getWeather().isEmpty()) {
            return null;
        }
        return response.getWeather().get(0).getDescription();
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