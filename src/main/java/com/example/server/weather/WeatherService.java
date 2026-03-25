package com.example.server.weather;


import com.example.server.localization.Localization;
import com.example.server.localization.LocalizationRepository;

public class WeatherService {

    private final WeatherAPIClient weatherAPIClient;
    private final LocalizationRepository localizationRepository;

    public WeatherService(WeatherAPIClient weatherAPIClient, LocalizationRepository localizationRepository) {
        this.weatherAPIClient = weatherAPIClient;
        this.localizationRepository = localizationRepository;
    }

    public Weather getCurrentWeather(Long localizationId) throws WeatherAPIClient.WeatherRetrievalException {
        Localization localization = localizationRepository.findOne(localizationId);

        if (localization == null) {
            throw new WeatherAPIClient.WeatherRetrievalException("Localization not found: " + localizationId);
        }

        return weatherAPIClient.getCurrentWeather(localization);
    }
}