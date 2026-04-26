package com.example.weather.location;

import com.example.weather.auth.AppUserRepository;
import com.example.weather.location.dto.CreateLocationRequest;
import com.example.weather.location.dto.LocationDTO;
import com.example.weather.location.dto.OrderByDTO;
import com.example.weather.location.dto.UpdateLocationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/my/locations")
@RequiredArgsConstructor
public class MyLocationsController {

    private final LocationService locationService;
    private final AppUserRepository appUserRepository;

    @GetMapping
    public List<LocationDTO> getMyLocations(Authentication authentication) {
        var ownerId = getCurrentUserId(authentication);

        return locationService.getPrivateLocations(ownerId)
                .stream()
                .map(LocationDTO::from)
                .toList();
    }

    @PostMapping
    public LocationDTO createMyLocation(
            Authentication authentication,
            @Valid @RequestBody CreateLocationRequest request
    ) {
        var ownerId = getCurrentUserId(authentication);

        var location = locationService.createPrivateLocation(
                ownerId,
                request.name(),
                request.longitude(),
                request.latitude(),
                request.region(),
                request.country()
        );

        return LocationDTO.from(location);
    }

    @PutMapping("/{id}")
    public LocationDTO updateMyLocation(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UpdateLocationRequest request
    ) {
        var ownerId = getCurrentUserId(authentication);

        var location = locationService.updatePrivateLocation(
                ownerId,
                id,
                request.name(),
                request.longitude(),
                request.latitude(),
                request.region(),
                request.country()
        );

        return LocationDTO.from(location);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMyLocation(Authentication authentication, @PathVariable Long id) {
        Long ownerId = getCurrentUserId(authentication);
        locationService.deletePrivateLocation(ownerId, id);
    }

    @PutMapping("/order")
    public List<LocationDTO> orderMyLocations(
            Authentication authentication,
            @RequestBody List<OrderByDTO> orders
    ) {
        var ownerId = getCurrentUserId(authentication);

        return locationService.savePrivateDisplayOrder(ownerId, orders)
                .stream()
                .map(LocationDTO::from)
                .toList();
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found");
        }

        var user = appUserRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));

        return user.getId();
    }
}
