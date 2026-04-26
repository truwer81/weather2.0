package com.example.weather.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ForecastResponseDTO(
        List<ForecastItemDTO> list,
        LocationDTO name
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ForecastItemDTO(
            long dt,
            MainDTO main,
            List<WeatherDTO> weather,
            WindDTO wind,
            CloudsDTO clouds,
            Double pop,
            RainDTO rain,
            SnowDTO snow,
            @JsonProperty("dt_txt") String dtTxt
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SnowDTO(
            @JsonProperty("3h") Double volumeLast3h
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MainDTO(
            double temp,
            @JsonProperty("feels_like") double feelsLike,
            int humidity,
            int pressure
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WeatherDTO(
            String main,
            String description,
            String icon
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WindDTO(
            double speed,
            int deg,
            Double gust
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RainDTO(
            @JsonProperty("3h") Double volumeLast3h
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LocationDTO(
            String name,
            String country,
            Integer timezone
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CloudsDTO(
            Double all
    ) {}
}
