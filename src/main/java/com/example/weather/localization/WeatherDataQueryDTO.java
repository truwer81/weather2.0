package com.example.weather.localization;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WeatherDataQueryDTO {
    private Long id;
    private String city;
    private String country;
    private String region;
    private double longitude;
    private double latitude;
}
