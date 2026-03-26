package com.example.weather.localization;

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
        if (longitude < -180 || longitude > 180 || latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Invalid longitude or latitude");
        }
        if (city == null || city.isBlank() || country == null || country.isBlank()) {
            throw new IllegalArgumentException("Invalid city or country");
        }
        if (region != null && region.isBlank()) {
            region = null;
        }

        var topOrderBy=0L;
        Localization lastLocalization = localizationRepository.findTopByOrderBySortOrderDesc().orElse(null);

        if (lastLocalization != null) {
            topOrderBy=lastLocalization.getSortOrder()+1;
        }

        var localization = new Localization(null, city, country, region, longitude, latitude, topOrderBy);
        return localizationRepository.save(localization);
    }

    @Transactional(readOnly = true)
    public List<Localization> getAllLocalizations() {
        return localizationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Localization getLocalization(long localizationId) {
        return localizationRepository.findById(localizationId).orElse(null);
    }

    @Transactional
    public void deleteLocalization(long localizationId) {
        var localization = localizationRepository.findById(localizationId)
                .orElseThrow(() -> new LocalizationNotFoundException(localizationId));
        localizationRepository.delete(localization);
    }

    @Transactional
    public List<Localization> saveDisplayOrder(List<OrderByDTO> orders) {
        boolean unique = orders.size() ==
                orders.stream()
                        .map(OrderByDTO::sortOrder)
                        .distinct()
                        .count();

        if (!unique) {
            throw new IllegalArgumentException("Order must be unique");
        }

        // temporary order
        for (OrderByDTO item : orders) {
            localizationRepository.updateOrderBy(item.localizationId(), -item.sortOrder() - 1000);
        }

        // order from OrderByDTO
        for (OrderByDTO item : orders) {
            localizationRepository.updateOrderBy(item.localizationId(), item.sortOrder());
        }
        return localizationRepository.findAll();
    }
}