package com.example.weather.weather;

import com.example.weather.localization.Localization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeatherRepository extends JpaRepository<Weather, Long> {

    Optional<Weather> findTopByLocalizationOrderByFetchedAtDesc(Localization localization);

    Optional<Weather> findTopByLocalizationIdOrderByFetchedAtDesc(Long localizationId);
}