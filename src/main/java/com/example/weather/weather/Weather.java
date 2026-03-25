package com.example.weather.weather;

import com.example.weather.localization.Localization;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "weather")
public class Weather {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "description")
    private String description;

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

    @Column(name = "clouds_all")
    private Double cloudsAll;

    @Column(name = "provider_timestamp")
    private LocalDateTime providerTimestamp;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "localization_id", nullable = false)
    private Localization localization;

    @PrePersist
    public void prePersist() {
        if (fetchedAt == null) {
            fetchedAt = LocalDateTime.now();
        }
    }

    @Override
    public String toString() {
        return "Weather{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", temperature=" + temperature +
                ", feelsLike=" + feelsLike +
                ", pressure=" + pressure +
                ", humidity=" + humidity +
                ", windSpeed=" + windSpeed +
                ", windDeg=" + windDeg +
                ", cloudsAll=" + cloudsAll +
                ", providerTimestamp=" + providerTimestamp +
                ", fetchedAt=" + fetchedAt +
                '}';
    }
}