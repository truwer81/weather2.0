package com.example.weather.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenWeatherResponseDTO {

    @JsonProperty("sys")
    private SystemInfo systemInfo;

    @JsonProperty("dt")
    private Long forecastTimestamp;

    @JsonProperty("name")
    private String cityName;

    @JsonProperty("weather")
    private List<WeatherDescription> weather;

    @JsonProperty("main")
    private MainInfo mainInfo;

    @JsonProperty("wind")
    private WindInfo windInfo;

    @JsonProperty("clouds")
    private CloudsInfo cloudsInfo;

    @JsonProperty("coord")
    private Coordinates coordinates;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SystemInfo {
        @JsonProperty("country")
        private String countryCode;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WeatherDescription {
        @JsonProperty("description")
        private String description;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MainInfo {
        @JsonProperty("temp")
        private Double temp;

        @JsonProperty("feels_like")
        private Double feelsLike;

        @JsonProperty("temp_min")
        private Double tempMin;

        @JsonProperty("temp_max")
        private Double tempMax;

        @JsonProperty("pressure")
        private Double pressure;

        @JsonProperty("humidity")
        private Double humidity;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WindInfo {
        @JsonProperty("speed")
        private Double windSpeed;

        @JsonProperty("deg")
        private Double windDeg;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CloudsInfo {
        @JsonProperty("all")
        private Double cloudsAll;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Coordinates {
        @JsonProperty("lon")
        private Double longitude;

        @JsonProperty("lat")
        private Double latitude;
    }
}