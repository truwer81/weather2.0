package com.example.weather.weather;

import com.example.weather.location.Location;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "forecast")
public class Forecast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "forecast_time")
    private Timestamp forecastTime;

    @Column(name = "temperature")
    private Double temperature;

    @Column(name = "feels_like")
    private Double feelsLike;

    @Column(name = "pressure")
    private Double pressure;

    @Column(name = "humidity")
    private Integer humidity;

    @Column(name = "wind_speed")
    private Double windSpeed;

    @Column(name = "wind_deg")
    private Double windDeg;

    @Column(name = "description")
    private String description;

    @Column(name = "clouds_all")
    private Double cloudsAll;

    @Column(name = "precipitation_probability")
    private Double precipitationProbability;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    @Column(name = "rain_volume")
    private Double rainVolume;

    @Column(name = "snow_volume")
    private Double snowVolume;

    @PrePersist
    public void prePersist() {
        if (fetchedAt == null) {
            fetchedAt = LocalDateTime.now();
        }
    }

    @Override
    public String toString() {
        return "Forecast{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", temperature=" + temperature +
                ", feelsLike=" + feelsLike +
                ", pressure=" + pressure +
                ", humidity=" + humidity +
                ", windSpeed=" + windSpeed +
                ", windDeg=" + windDeg +
                ", cloudsAll=" + cloudsAll +
                ", forecastTime=" + forecastTime +
                ", fetchedAt=" + fetchedAt +
                ", rainVolume=" + rainVolume +
                ", snowVolume=" + snowVolume +
                ", precipitationProbability=" + precipitationProbability +
                '}';
    }
}
