package com.example.weather.weather;

import com.example.weather.localization.Localization;
import com.example.weather.localization.LocalizationService;
import com.example.weather.weather.dto.OpenWeatherResponseDTO;
import com.example.weather.weather.dto.WeatherDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private static final long WEATHER_CACHE_HOURS = 3;

    private final WeatherRepository weatherRepository;
    private final LocalizationService localizationService;
    private final WeatherApiClient weatherApiClient;

    @Transactional
    public WeatherDTO getWeatherForLocalization(Long localizationId) throws WeatherApiClient.WeatherRetrievalException {
        Localization localization = localizationService.getLocalization(localizationId);

        return weatherRepository.findTopByLocalizationIdOrderByFetchedAtDesc(localizationId)
                .filter(weather -> !isExpired(weather))
                .map(WeatherMapper::toDTO)
                .orElseGet(() -> fetchAndSaveWeather(localization));
    }

    private WeatherDTO fetchAndSaveWeather(Localization localization) {
        try {
            OpenWeatherResponseDTO response = weatherApiClient.getCurrentWeather(localization);
            Weather weather = WeatherMapper.toEntity(localization, response);
            Weather savedWeather = weatherRepository.save(weather);
            return WeatherMapper.toDTO(savedWeather);
        } catch (WeatherApiClient.WeatherRetrievalException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isExpired(Weather weather) {
        if (weather.getFetchedAt() == null) {
            return true;
        }

        return weather.getFetchedAt().isBefore(LocalDateTime.now().minusHours(WEATHER_CACHE_HOURS));
    }
}
