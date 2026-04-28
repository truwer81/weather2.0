package com.example.weather.weather;

import com.example.weather.common.LocationNotFoundException;
import com.example.weather.location.Location;
import com.example.weather.location.LocationService;
import com.example.weather.weather.dto.ForecastDTO;
import com.example.weather.weather.dto.WeatherDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpenWeatherServiceTest {

    @Mock
    private WeatherRepository weatherRepository;

    @Mock
    private ForecastRepository forecastRepository;

    @Mock
    private LocationService locationService;

    @Mock
    private OpenWeatherApiClient openWeatherApiClient;

    @InjectMocks
    private OpenWeatherService openWeatherService;

    @Test
    void getWeatherForOwnedLocation_returnsCachedWeatherForOwnedLocation() throws Exception {
        var location = new Location(24L, "Berlin", "Germany", "Berlin", 13.4050, 52.5200, 1L, null);
        var cachedWeather = new Weather(
                1L,
                "clear sky",
                22.5,
                21.0,
                1012.0,
                60,
                4.5,
                180.0,
                10.0,
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now().minusMinutes(5),
                location
        );

        when(locationService.getOwnedLocation(10L, 24L)).thenReturn(location);
        when(weatherRepository.findTopByLocationIdOrderByFetchedAtDesc(24L)).thenReturn(Optional.of(cachedWeather));

        WeatherDTO result = openWeatherService.getWeatherForOwnedLocation(10L, 24L);

        assertThat(result.locationId()).isEqualTo(24L);
        assertThat(result.name()).isEqualTo("Berlin");
        assertThat(result.description()).isEqualTo("clear sky");
        verify(locationService).getOwnedLocation(10L, 24L);
        verify(openWeatherApiClient, never()).getCurrentWeather(location);
    }

    @Test
    void getForecastsForOwnedLocation_returnsCachedForecastForOwnedLocation() throws Exception {
        var location = new Location(24L, "Berlin", "Germany", "Berlin", 13.4050, 52.5200, 1L, null);
        var cachedForecast = new Forecast(
                1L,
                location,
                Timestamp.valueOf(LocalDateTime.now().plusHours(3)),
                18.0,
                17.0,
                1010.0,
                70,
                5.5,
                200.0,
                "few clouds",
                20.0,
                0.3,
                LocalDateTime.now().minusMinutes(10),
                0.0,
                0.0
        );

        when(locationService.getOwnedLocation(10L, 24L)).thenReturn(location);
        when(forecastRepository.findAllByLocationIdOrderByForecastTimeAsc(24L)).thenReturn(List.of(cachedForecast));

        List<ForecastDTO> result = openWeatherService.getForecastsForOwnedLocation(10L, 24L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().description()).isEqualTo("few clouds");
        assertThat(result.getFirst().temperature()).isEqualTo(18.0);
        verify(locationService).getOwnedLocation(10L, 24L);
        verify(openWeatherApiClient, never()).getForecast(location);
    }

    @Test
    void getWeatherForOwnedLocation_throwsNotFoundForForeignLocation() {
        when(locationService.getOwnedLocation(10L, 99L)).thenThrow(new LocationNotFoundException(99L));

        assertThatThrownBy(() -> openWeatherService.getWeatherForOwnedLocation(10L, 99L))
                .isInstanceOf(LocationNotFoundException.class)
                .hasMessageContaining("99");

        verify(weatherRepository, never()).findTopByLocationIdOrderByFetchedAtDesc(99L);
    }
}
