package com.example.weather.weather;


import com.example.weather.localization.LocalizationRepository;

public class WeatherService {

    private final WeatherAPIClient weatherAPIClient;
    private final LocalizationRepository localizationRepository;

    public WeatherService(WeatherAPIClient weatherAPIClient, LocalizationRepository localizationRepository) {
        this.weatherAPIClient = weatherAPIClient;
        this.localizationRepository = localizationRepository;
    }

    public Weather getCurrentWeather(Long localizationId) throws WeatherAPIClient.WeatherRetrievalException {
        var localization = localizationRepository.findById(localizationId)
                .orElseThrow(() -> new RuntimeException("Localization not found: " + localizationId));

        if (localization == null) {
            throw new WeatherAPIClient.WeatherRetrievalException("Localization not found: " + localizationId);
        }

        return weatherAPIClient.getCurrentWeather(localization);
    }
}