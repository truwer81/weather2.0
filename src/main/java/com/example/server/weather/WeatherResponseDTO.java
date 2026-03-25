package com.example.server.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherResponseDTO {

    @JsonProperty("sys")
    private SystemInfo systemInfo; // Klasa pomocnicza dla danych sys

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SystemInfo {
        @JsonProperty("country")
        private String countryCode;
    }

    @JsonProperty("dt")
    private Long forecastTimestamp;
    @JsonProperty("name")
    private String cityName;

    @JsonProperty("weather")
    private List<WeatherDescription> weather; // Klasa pomocnicza dla danych weather

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WeatherDescription {
        @JsonProperty("description")
        private String description;
    }

    @JsonProperty("main")
    private MainInfo mainInfo; // Klasa pomocnicza dla danych main

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MainInfo {
        @JsonProperty("temp")
        private Float temp;
        @JsonProperty("feels_like")
        private Float feelsLike;
        @JsonProperty("temp_min")
        private Float tempMin;
        @JsonProperty("temp_max")
        private Float tempMax;
        @JsonProperty("pressure")
        private Float pressure;
        @JsonProperty("humidity")
        private Float humidity;
    }

    @JsonProperty("wind")
    private WindInfo windInfo; // Klasa pomocnicza dla danych wind

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WindInfo {
        @JsonProperty("speed")
        private Float windSpeed;
        @JsonProperty("deg")
        private Float windDeg;
    }

    @JsonProperty("clouds")
    private CloudsInfo cloudsInfo; // Klasa pomocnicza dla danych clouds

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CloudsInfo {
        @JsonProperty("all")
        private Float cloudsAll;
    }

    @JsonProperty("coord")
    private Coordinates coordinates; // Klasa pomocnicza dla danych coord
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Coordinates {
        @JsonProperty("lon")
        private Float longitude;
        @JsonProperty("lat")
        private Float latitude;
    }


}

