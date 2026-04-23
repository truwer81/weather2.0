package com.example.weather.localization;

import com.example.weather.auth.AppUser;
import com.example.weather.auth.AppUserRepository;
import com.example.weather.localization.dto.CreateLocalizationRequest;
import com.example.weather.localization.dto.LocalizationDTO;
import com.example.weather.localization.dto.OrderByDTO;
import com.example.weather.localization.dto.UpdateLocalizationRequest;
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

    private final LocalizationService localizationService;
    private final AppUserRepository appUserRepository;

    @GetMapping
    public List<LocalizationDTO> getMyLocations(Authentication authentication) {
        var ownerId = getCurrentUserId(authentication);

        return localizationService.getPrivateLocalizations(ownerId)
                .stream()
                .map(LocalizationDTO::from)
                .toList();
    }

    @PostMapping
    public LocalizationDTO createMyLocation(
            Authentication authentication,
            @Valid @RequestBody CreateLocalizationRequest request
    ) {
        var ownerId = getCurrentUserId(authentication);

        var localization = localizationService.createPrivateLocalization(
                ownerId,
                request.city(),
                request.longitude(),
                request.latitude(),
                request.region(),
                request.country()
        );

        return LocalizationDTO.from(localization);
    }

    @PutMapping("/{id}")
    public LocalizationDTO updateMyLocation(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UpdateLocalizationRequest request
    ) {
        var ownerId = getCurrentUserId(authentication);

        var localization = localizationService.updatePrivateLocalization(
                ownerId,
                id,
                request.city(),
                request.longitude(),
                request.latitude(),
                request.region(),
                request.country()
        );

        return LocalizationDTO.from(localization);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMyLocation(Authentication authentication, @PathVariable Long id) {
        Long ownerId = getCurrentUserId(authentication);
        localizationService.deletePrivateLocalization(ownerId, id);
    }

    @PutMapping("/order")
    public List<LocalizationDTO> orderMyLocations(
            Authentication authentication,
            @RequestBody List<OrderByDTO> orders
    ) {
        var ownerId = getCurrentUserId(authentication);

        return localizationService.savePrivateDisplayOrder(ownerId, orders)
                .stream()
                .map(LocalizationDTO::from)
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
