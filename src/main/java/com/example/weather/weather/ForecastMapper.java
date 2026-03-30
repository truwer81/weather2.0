package com.example.weather.weather;

import com.example.weather.weather.dto.ForecastDTO;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public final class ForecastMapper {

    private ForecastMapper() {}

    public static List<ForecastDTO> toForecastDTO(List<Forecast> forecasts) {
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
                forecast.getRainVolume(),
                forecast.getSnowVolume(),
                forecast.getDescription(),
                defaultDouble(forecast.getPrecipitationProbability()),
                forecast.getCloudsAll()
        );
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    private static double defaultDouble(Number value) {
        return value != null ? value.doubleValue() : 0.0;
    }
}