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
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;
    private final LocationMapper locationMapper;

    @PostMapping
    public LocationDTO createLocation(@Valid @RequestBody CreateLocationRequest request) {
        Location location = locationService.createSharedLocation(
                request.name(),
                request.longitude(),
                request.latitude(),
                request.region(),
                request.country()
        );

        return locationMapper.toDTO(location);
    }

    @GetMapping
    public List<LocationDTO> getSharedLocations() {
        return locationMapper.toDTOList(locationService.getSharedLocations());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLocation(@PathVariable Long id) {
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
        return locationMapper.toDTO(location);
    }

    @PutMapping("/order")
    public List<LocationDTO> orderLocations(@RequestBody List<OrderByDTO> orders) {
        return locationMapper.toDTOList(locationService.saveDisplayOrder(orders));
    }
}
