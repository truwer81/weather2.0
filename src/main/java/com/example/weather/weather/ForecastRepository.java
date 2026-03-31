package com.example.weather.weather;

import com.example.weather.localization.Localization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ForecastRepository extends JpaRepository<Forecast, Long> {

    void deleteByLocalizationId(Long localizationId);

    List<Forecast> findAllByLocalizationIdOrderByForecastTimeAsc(Long localizationId);
}
