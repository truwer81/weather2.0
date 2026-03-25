package com.example.server.localization;

import java.util.List;

public class LocalizationService {

    private LocalizationRepository localizationRepository;

    public LocalizationService(LocalizationRepository localizationRepository) {
        this.localizationRepository = localizationRepository;
    }

    public Localization createLocalization(String city, double longitude, double latitude, String region, String country) {
        if (longitude < -180 || longitude > 180 || latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Invalid longitude or latitude");
        }
        if (city == null || city.isBlank() || country == null || country.isBlank()) {
            throw new IllegalArgumentException("Invalid city or country");
        }
        if (region != null && region.isBlank()) {
            region = null;
        }

        Localization localization = new Localization(null, city, country, region, longitude, latitude);

        return localizationRepository.save(localization);
    }

    public List<Localization> getAllLocalizations() {
        return localizationRepository.findAll();
    }

    public Localization getLocalization(long localizationId) {
        return localizationRepository.findOne(localizationId);
    }
}