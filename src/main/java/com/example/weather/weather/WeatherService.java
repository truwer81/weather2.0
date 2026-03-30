package com.example.weather.weather;

import com.example.weather.localization.Localization;
import com.example.weather.localization.LocalizationService;
import com.example.weather.weather.dto.ForecastDTO;
import com.example.weather.weather.dto.ForecastResponseDTO;
import com.example.weather.weather.dto.WeatherDTO;
import com.example.weather.weather.dto.WeatherResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private static final long WEATHER_CACHE_HOURS = 1;

    private final WeatherRepository weatherRepository;
    private final ForecastRepository forecastRepository;

    private final LocalizationService localizationService;
    private final OpenWeatherApiClient openWeatherApiClient;

    @Transactional
    public WeatherDTO getWeatherForLocalization(Long localizationId) throws OpenWeatherApiClient.WeatherRetrievalException {
        var localization = localizationService.getLocalization(localizationId);

        return weatherRepository.findTopByLocalizationIdOrderByFetchedAtDesc(localizationId)
                .filter(weather -> !isExpired(weather))
                .map(WeatherMapper::toDTO)
                .orElseGet(() -> fetchAndSaveWeather(localization));
    }

    @Transactional
    public List<ForecastDTO> getForecastsForLocalization(Long localizationId) {
        var localization = localizationService.getLocalization(localizationId);

        var forecasts = forecastRepository
                .findAllByLocalizationIdOrderByForecastTimeAsc(localizationId);

        if (!isExpired(forecasts.getFirst())) {
            return ForecastMapper.toForecastDTO(forecasts);
        }

        return fetchAndSaveForecast(localization);
    }

    private WeatherDTO fetchAndSaveWeather(Localization localization) {
        try {
            WeatherResponseDTO response = openWeatherApiClient.getCurrentWeather(localization);
            var weather = WeatherMapper.toEntity(localization, response);
            var savedWeather = weatherRepository.save(weather);
            return WeatherMapper.toDTO(savedWeather);
        } catch (OpenWeatherApiClient.WeatherRetrievalException e) {
            throw new RuntimeException(e);
        }
    }

    private List<ForecastDTO> fetchAndSaveForecast(Localization localization) {
        try {
            ForecastResponseDTO response = openWeatherApiClient.getForecast(localization);

            var forecasts = WeatherMapper.toEntities(localization, response);

            forecastRepository.deleteByLocalization(localization.getId());

            var savedForecast = forecastRepository.saveAll(forecasts);

            return ForecastMapper.toForecastDTO(savedForecast);

        } catch (OpenWeatherApiClient.WeatherRetrievalException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isExpired(Weather weather) {
        if (weather.getFetchedAt() == null) {
            return true;
        }
        return weather.getFetchedAt()
                .isBefore(LocalDateTime.now().minusHours(WEATHER_CACHE_HOURS));
    }

    private boolean isExpired(Forecast forecast) {
        if (forecast.getFetchedAt() == null) {
            return true;
        }
        return forecast.getFetchedAt()
                .isBefore(LocalDateTime.now().minusHours(WEATHER_CACHE_HOURS));
    }
}
