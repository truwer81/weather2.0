package com.example.weather.weather;

import com.example.weather.common.ExternalServiceException;
import com.example.weather.common.OpenWeatherClientException;
import com.example.weather.location.Location;
import com.example.weather.location.LocationService;
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
    private final LocationService locationService;
    private final OpenWeatherApiClient openWeatherApiClient;

    @Transactional
    public WeatherDTO getWeatherForLocation(Long locationId) {
        var location = locationService.getSharedLocation(locationId);

        return weatherRepository.findTopByLocationIdOrderByFetchedAtDesc(locationId)
                .filter(weather -> !isWeatherExpired(weather))
                .map(weather -> {
                    log.info("Using cached weather for location {}", locationId);
                    return WeatherMapper.toDTO(weather);
                })
                .orElseGet(() -> fetchAndSaveWeather(location));
    }

    @Transactional
    public List<ForecastDTO> getForecastsForLocation(Long locationId) {
        var location = locationService.getSharedLocation(locationId);
        var forecasts = forecastRepository.findAllByLocationIdOrderByForecastTimeAsc(locationId);

        if (forecasts.isEmpty()) {
            log.info("No cached forecast found for location {}, fetching from API", locationId);
            return fetchAndSaveForecast(location);
        }

        var firstForecast = forecasts.getFirst();

        if (!isForecastExpired(firstForecast)) {
            log.info("Using cached forecast for location {}", locationId);
            return ForecastMapper.toDTOList(forecasts);
        }

        log.info("Cached forecast expired for location {}, fetching from API", locationId);
        return fetchAndSaveForecast(location);
    }

    private WeatherDTO fetchAndSaveWeather(Location location) {
        try {
            log.info("Fetching weather from OpenWeather for location {}", location.getId());

            var response = openWeatherApiClient.getCurrentWeather(location);
            var weather = WeatherMapper.toEntity(location, response);
            var savedWeather = weatherRepository.save(weather);

            return WeatherMapper.toDTO(savedWeather);
        } catch (OpenWeatherClientException e) {
            throw new ExternalServiceException("Could not fetch weather from OpenWeather", e);
        }
    }

    private List<ForecastDTO> fetchAndSaveForecast(Location location) {
        try {
            log.info("Fetching forecast from OpenWeather for location {}", location.getId());

            var response = openWeatherApiClient.getForecast(location);
            var forecasts = ForecastMapper.toEntities(location, response);

            forecastRepository.deleteByLocationId(location.getId());
            var savedForecasts = forecastRepository.saveAll(forecasts);

            return ForecastMapper.toDTOList(savedForecasts);
        } catch (OpenWeatherClientException e) {
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