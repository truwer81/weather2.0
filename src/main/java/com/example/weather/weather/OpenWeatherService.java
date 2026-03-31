package com.example.weather.weather;

import com.example.weather.common.ExternalServiceException;
import com.example.weather.localization.Localization;
import com.example.weather.localization.LocalizationService;
import com.example.weather.weather.dto.ForecastDTO;
import com.example.weather.weather.dto.WeatherDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenWeatherService {

    private static final long WEATHER_CACHE_HOURS = 1;
    private static final long FORECAST_CACHE_HOURS = 1;

    private final WeatherRepository weatherRepository;
    private final ForecastRepository forecastRepository;
    private final LocalizationService localizationService;
    private final OpenWeatherApiClient openWeatherApiClient;

    @Transactional
    public WeatherDTO getWeatherForLocalization(Long localizationId) {
        var localization = localizationService.getLocalization(localizationId);

        return weatherRepository.findTopByLocalizationIdOrderByFetchedAtDesc(localizationId)
                .filter(weather -> !isWeatherExpired(weather))
                .map(weather -> {
                    log.info("Using cached weather for localization {}", localizationId);
                    return WeatherMapper.toDTO(weather);
                })
                .orElseGet(() -> fetchAndSaveWeather(localization));
    }

    @Transactional
    public List<ForecastDTO> getForecastsForLocalization(Long localizationId) {
        var localization = localizationService.getLocalization(localizationId);
        var forecasts = forecastRepository.findAllByLocalizationIdOrderByForecastTimeAsc(localizationId);

        if (forecasts.isEmpty()) {
            log.info("No cached forecast found for localization {}, fetching from API", localizationId);
            return fetchAndSaveForecast(localization);
        }

        var firstForecast = forecasts.getFirst();

        if (!isForecastExpired(firstForecast)) {
            log.info("Using cached forecast for localization {}", localizationId);
            return ForecastMapper.toDTOList(forecasts);
        }

        log.info("Cached forecast expired for localization {}, fetching from API", localizationId);
        return fetchAndSaveForecast(localization);
    }

    private WeatherDTO fetchAndSaveWeather(Localization localization) {
        try {
            log.info("Fetching weather from OpenWeather for localization {}", localization.getId());

            var response = openWeatherApiClient.getCurrentWeather(localization);
            var weather = WeatherMapper.toEntity(localization, response);
            var savedWeather = weatherRepository.save(weather);

            return WeatherMapper.toDTO(savedWeather);
        } catch (OpenWeatherApiClient.WeatherRetrievalException e) {
            throw new ExternalServiceException("Could not fetch weather from OpenWeather", e);
        }
    }

    private List<ForecastDTO> fetchAndSaveForecast(Localization localization) {
        try {
            log.info("Fetching forecast from OpenWeather for localization {}", localization.getId());

            var response = openWeatherApiClient.getForecast(localization);
            var forecasts = ForecastMapper.toEntities(localization, response);

            forecastRepository.deleteByLocalizationId(localization.getId());
            var savedForecasts = forecastRepository.saveAll(forecasts);

            return ForecastMapper.toDTOList(savedForecasts);
        } catch (OpenWeatherApiClient.WeatherRetrievalException e) {
            throw new ExternalServiceException("Could not fetch forecast from OpenWeather", e);
        }
    }

    private boolean isWeatherExpired(Weather weather) {
        if (weather.getFetchedAt() == null) {
            return true;
        }

        return weather.getFetchedAt()
                .isBefore(LocalDateTime.now().minusHours(WEATHER_CACHE_HOURS));
    }

    private boolean isForecastExpired(Forecast forecast) {
        if (forecast.getFetchedAt() == null) {
            return true;
        }

        return forecast.getFetchedAt()
                .isBefore(LocalDateTime.now().minusHours(FORECAST_CACHE_HOURS));
    }
}