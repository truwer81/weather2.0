package com.example.weather.location;

import com.example.weather.location.dto.CreateLocationRequest;
import com.example.weather.location.dto.LocationDTO;
import com.example.weather.location.dto.OrderByDTO;
import com.example.weather.location.dto.UpdateLocationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    public LocationDTO createLocation(@Valid @RequestBody CreateLocationRequest request) {
        Location location = locationService.createSharedLocation(
                request.name(),
                request.longitude(),
                request.latitude(),
                request.region(),
                request.country()
        );

        return LocationDTO.from(location);
    }

    @GetMapping
    public List<LocationDTO> getAllCities() {
        return locationService.getSharedLocations()
                .stream()
                .map(LocationDTO::from)
                .toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCity(@PathVariable Long id) {
        locationService.deleteLocation(id);
    }

    @PutMapping("/{id}")
    public LocationDTO updateLocation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLocationRequest request
    ) {
        Location location = locationService.updateLocation(
                id,
                request.name(),
                request.longitude(),
                request.latitude(),
                request.region(),
                request.country()
        );
        return LocationDTO.from(location);
    }

    @PutMapping("/order")
    public List<LocationDTO> orderCities(@RequestBody List<OrderByDTO> orders) {
        return locationService.saveDisplayOrder(orders)
                .stream()
                .map(LocationDTO::from)
                .toList();
    }
}