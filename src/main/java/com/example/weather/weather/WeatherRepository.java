package com.example.weather.weather;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeatherRepository extends JpaRepository<Weather, Long> {


    Optional<Weather> findTopByLocationIdOrderByFetchedAtDesc(Long locationId);
}