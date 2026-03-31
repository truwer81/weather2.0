package com.example.weather.localization;

import com.example.weather.common.BadRequestException;
import com.example.weather.common.LocalizationNotFoundException;
import com.example.weather.localization.dto.OrderByDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocalizationService {

    private final LocalizationRepository localizationRepository;

    @Transactional
    public Localization createLocalization(String city, Double longitude, Double latitude, String region, String country) {
        validateCoordinates(longitude, latitude);
        validateRequiredFields(city, country);

        if (region != null && region.isBlank()) {
            region = null;
        }

        long topSortOrder = localizationRepository.findTopByOrderBySortOrderDesc()
                .map(Localization::getSortOrder)
                .map(sortOrder -> sortOrder + 1)
                .orElse(0L);

        var localization = new Localization(null, city, country, region, longitude, latitude, topSortOrder);
        return localizationRepository.save(localization);
    }

    @Transactional(readOnly = true)
    public List<Localization> getAllLocalizations() {
        return localizationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Localization getLocalization(Long id) {
        return localizationRepository.findById(id)
                .orElseThrow(() -> new LocalizationNotFoundException(id));
    }

    @Transactional
    public void deleteLocalization(long localizationId) {
        var localization = localizationRepository.findById(localizationId)
                .orElseThrow(() -> new LocalizationNotFoundException(localizationId));

        localizationRepository.delete(localization);
    }

    @Transactional
    public List<Localization> saveDisplayOrder(List<OrderByDTO> orders) {
        validateOrders(orders);

        for (OrderByDTO item : orders) {
            localizationRepository.updateSortOrder(item.localizationId(), -item.sortOrder() - 1000);
        }

        for (OrderByDTO item : orders) {
            localizationRepository.updateSortOrder(item.localizationId(), item.sortOrder());
        }

        return localizationRepository.findAll();
    }

    private void validateCoordinates(Double longitude, Double latitude) {
        if (longitude == null || latitude == null) {
            throw new BadRequestException("Longitude and latitude are required");
        }

        if (longitude < -180 || longitude > 180) {
            throw new BadRequestException("Longitude must be between -180 and 180");
        }

        if (latitude < -90 || latitude > 90) {
            throw new BadRequestException("Latitude must be between -90 and 90");
        }
    }

    private void validateRequiredFields(String city, String country) {
        if (city == null || city.isBlank()) {
            throw new BadRequestException("City is required");
        }

        if (country == null || country.isBlank()) {
            throw new BadRequestException("Country is required");
        }
    }

    private void validateOrders(List<OrderByDTO> orders) {
        if (orders == null || orders.isEmpty()) {
            throw new BadRequestException("Display order payload cannot be empty");
        }

        boolean unique = orders.size() ==
                orders.stream()
                        .map(OrderByDTO::sortOrder)
                        .distinct()
                        .count();

        if (!unique) {
            throw new BadRequestException("Sort order values must be unique");
        }
    }
}