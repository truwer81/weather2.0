package com.example.weather.localization;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocalizationService {

    private final LocalizationRepository localizationRepository;

    @Transactional
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
        return localizationRepository.findById(localizationId).orElse(null);
    }
}