package com.example.weather.location;

import com.example.weather.location.dto.LocationSearchResultDTO;
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
public class LocationSearchController {

    private final LocationSearchService locationSearchService;

    @GetMapping("/search")
    public List<LocationSearchResultDTO> search(@RequestParam("q") @NotBlank String query) {
        return locationSearchService.search(query);
    }
}
