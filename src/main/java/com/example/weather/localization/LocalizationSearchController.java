package com.example.weather.localization;

import com.example.weather.localization.dto.LocalizationSearchResultDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Validated
public class LocalizationSearchController {

    private final LocalizationSearchService localizationSearchService;

    @GetMapping("/search")
    public List<LocalizationSearchResultDTO> search(@RequestParam("q") @NotBlank String query) {
        return localizationSearchService.search(query);
    }
}
