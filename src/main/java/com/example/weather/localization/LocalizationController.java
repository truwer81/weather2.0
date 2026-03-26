package com.example.weather.localization;

import com.example.weather.localization.dto.CreateLocalizationRequest;
import com.example.weather.localization.dto.LocalizationDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
public class LocalizationController {

    private final LocalizationService localizationService;

    @PostMapping
    public LocalizationDTO createCity(@Valid @RequestBody CreateLocalizationRequest request) {
        Localization localization = localizationService.createLocalization(
                request.city(),
                request.longitude(),
                request.latitude(),
                request.region(),
                request.country()
        );

        return LocalizationDTO.from(localization);
    }

    @GetMapping
    public List<LocalizationDTO> getAllCities() {
        return localizationService.getAllLocalizations()
                .stream()
                .map(LocalizationDTO::from)
                .toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCity(@PathVariable Long id) {
        localizationService.deleteLocalization(id);
    }
}