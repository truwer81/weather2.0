package com.example.server.weather;

import com.example.server.localization.Localization;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Weather {

    private String description;
    private Float temp;
    private Float feelsLike;
    private Float pressure;
    private Float humidity;
    private Float windSpeed;
    private Float windDeg;
    private Float cloudsAll;
    private Long forecastTimestamp;
    private Localization localization;

    @Override
    public String toString() {
        return "Weather" +
                "{\ndescription='" + description +
                "\ntemp=" + temp +
                "\nfeelsLike=" + feelsLike +
                "\npressure=" + pressure +
                "\nhumidity=" + humidity +
                "\nwindSpeed=" + windSpeed +
                "\nwindDeg=" + windDeg +
                "\ncloudsAll=" + cloudsAll +
                "\nforecastTimestamp=" + forecastTimestamp +
                "\nlocalization=" + localization +
                '}';
    }
}

