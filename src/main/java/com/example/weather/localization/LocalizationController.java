package com.example.weather.localization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

// @RequiredArgsConstructor - zamiast konstuktora
public class LocalizationController {

    private ObjectMapper objectMapper;
    private LocalizationService localizationService;

    public LocalizationController(ObjectMapper objectMapper, LocalizationService localizationService) {
        this.objectMapper = objectMapper;
        this.localizationService = localizationService;
    }

    // POST /localizations
    public String createLocalization(String json) {
        try {
            WeatherDataQueryDTO model = objectMapper.readValue(json, WeatherDataQueryDTO.class);
            String city = model.getCity();
            var longitude = model.getLongitude();
            var latitude = model.getLatitude();
            String region = model.getRegion();
            String country = model.getCountry();
            Localization localization = localizationService.createLocalization(city, longitude, latitude, region, country);
            WeatherDataQueryDTO weatherDataQueryDTO = asDTO(localization);
            return objectMapper.writeValueAsString(weatherDataQueryDTO);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            return "{\"error\": \"Invalid data\"}"; // http 400
        } catch (Exception e) {
            return "{\"error\": \"Internal server error\"}"; // http 500
        }
    }

    // GET /localizations
    public String getLocalizations() throws JsonProcessingException {
        try {
            List<Localization> localizations = localizationService.getAllLocalizations();
            return objectMapper.writeValueAsString(localizations);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Internal server error\"}"; //http 400
        } catch (Exception e) {
            return "{\"error\": \"Internal server error\"}"; // http 500
        }
    }

    // GET /localizations/{localizationId}
    public String getLocalization(long localizationId) throws JsonProcessingException {
        try {
            Localization localizations = localizationService.getLocalization(localizationId);
            return objectMapper.writeValueAsString(localizations);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Internal server error\"}"; //http 400
        } catch (Exception e) {
            return "{\"error\": \"Internal server error\"}"; // http 500
        }
    }


    public WeatherDataQueryDTO asDTO(Localization localization) {
        return new WeatherDataQueryDTO(
                localization.getId(),
                localization.getCity(),
                localization.getCountry(),
                localization.getRegion(),
                localization.getLongitude(),
                localization.getLatitude()
        );
    }
}
