package com.example.weather.localization;

import com.example.weather.auth.AppUser;
import com.example.weather.auth.AppUserRepository;
import com.example.weather.common.BadRequestException;
import com.example.weather.common.LocalizationNotFoundException;
import com.example.weather.localization.dto.OrderByDTO;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LocalizationService {

    private final LocalizationRepository localizationRepository;
    private final AppUserRepository appUserRepository;
    private final EntityManager entityManager;

    @Transactional
    public Localization createLocalization(String city, Double longitude, Double latitude, String region, String country) {
        validateCoordinates(longitude, latitude);
        validateRequiredFields(city, country);

        if (region != null && region.isBlank()) {
            region = null;
        }

        long topSortOrder = localizationRepository.findTopByOwnerIsNullOrderBySortOrderDesc()
                .map(Localization::getSortOrder)
                .map(sortOrder -> sortOrder + 1)
                .orElse(0L);

        var localization = new Localization(null, city, country, region, longitude, latitude, topSortOrder, null);
        return localizationRepository.save(localization);
    }

    @Transactional(readOnly = true)
    public List<Localization> getAllLocalizations() {
        return localizationRepository.findAllByOwnerIsNullOrderBySortOrderAsc();
    }

    @Transactional(readOnly = true)
    public List<Localization> getPrivateLocalizations(Long ownerId) {
        return localizationRepository.findAllByOwnerIdOrderBySortOrderAsc(ownerId);
    }

    @Transactional(readOnly = true)
    public Localization getLocalization(Long id) {
        return getSharedLocalization(id);
    }

    @Transactional
    public void deleteLocalization(long localizationId) {
        var localization = getSharedLocalization(localizationId);

        localizationRepository.delete(localization);
    }

    @Transactional
    public void deletePrivateLocalization(long ownerId, long localizationId) {
        var localization = getOwnedLocalization(ownerId, localizationId);

        localizationRepository.delete(localization);
    }

    @Transactional
    public Localization updateLocalization(
            long localizationId,
            String city,
            Double longitude,
            Double latitude,
            String region,
            String country
    ) {
        validateCoordinates(longitude, latitude);
        validateRequiredFields(city, country);

        var localization = getSharedLocalization(localizationId);

        localization.setCity(city.trim());
        localization.setCountry(country.trim());
        localization.setRegion(region == null || region.isBlank() ? null : region.trim());
        localization.setLongitude(longitude);
        localization.setLatitude(latitude);

        return localizationRepository.save(localization);
    }

    @Transactional
    public Localization createPrivateLocalization(
            Long ownerId,
            String city,
            Double longitude,
            Double latitude,
            String region,
            String country
    ) {
        validateCoordinates(longitude, latitude);
        validateRequiredFields(city, country);

        if (region != null && region.isBlank()) {
            region = null;
        }

        long topSortOrder = localizationRepository.findTopByOwnerIdOrderBySortOrderDesc(ownerId)
                .map(Localization::getSortOrder)
                .map(sortOrder -> sortOrder + 1)
                .orElse(0L);

        AppUser owner = appUserRepository.findById(ownerId)
                .orElseThrow(() -> new LocalizationNotFoundException(ownerId));

        var localization = new Localization(null, city, country, region, longitude, latitude, topSortOrder, owner);
        return localizationRepository.save(localization);
    }

    @Transactional
    public Localization updatePrivateLocalization(
            long ownerId,
            long localizationId,
            String city,
            Double longitude,
            Double latitude,
            String region,
            String country
    ) {
        validateCoordinates(longitude, latitude);
        validateRequiredFields(city, country);

        var localization = getOwnedLocalization(ownerId, localizationId);

        localization.setCity(city.trim());
        localization.setCountry(country.trim());
        localization.setRegion(region == null || region.isBlank() ? null : region.trim());
        localization.setLongitude(longitude);
        localization.setLatitude(latitude);

        return localizationRepository.save(localization);
    }


    @Transactional
    public List<Localization> saveDisplayOrder(List<OrderByDTO> orders) {
        validateOrders(orders);
        validateSharedOrderScope(orders);

        for (OrderByDTO item : orders) {
            localizationRepository.updateSortOrder(item.localizationId(), -item.sortOrder() - 1000);
        }

        for (OrderByDTO item : orders) {
            localizationRepository.updateSortOrder(item.localizationId(), item.sortOrder());
        }

        entityManager.flush();
        entityManager.clear();

        return localizationRepository.findAllByOwnerIsNullOrderBySortOrderAsc();
    }

    @Transactional
    public List<Localization> savePrivateDisplayOrder(Long ownerId, List<OrderByDTO> orders) {
        validateOrders(orders);
        validatePrivateOrderScope(ownerId, orders);

        for (OrderByDTO item : orders) {
            localizationRepository.updateSortOrder(item.localizationId(), -item.sortOrder() - 1000);
        }

        for (OrderByDTO item : orders) {
            localizationRepository.updateSortOrder(item.localizationId(), item.sortOrder());
        }

        entityManager.flush();
        entityManager.clear();

        return localizationRepository.findAllByOwnerIdOrderBySortOrderAsc(ownerId);
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

    private void validateSharedOrderScope(List<OrderByDTO> orders) {
        Set<Long> uniqueIds = orders.stream()
                .map(OrderByDTO::localizationId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        if (uniqueIds.contains(null)) {
            throw new BadRequestException("Localization id is required for display order");
        }

        long sharedCount = localizationRepository.countByIdInAndOwnerIsNull(uniqueIds);

        if (sharedCount != uniqueIds.size()) {
            throw new BadRequestException("Display order payload can contain shared localizations only");
        }
    }

    private void validatePrivateOrderScope(Long ownerId, List<OrderByDTO> orders) {
        Set<Long> uniqueIds = orders.stream()
                .map(OrderByDTO::localizationId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        if (uniqueIds.contains(null)) {
            throw new BadRequestException("Localization id is required for display order");
        }

        long ownedCount = localizationRepository.countByIdInAndOwnerId(uniqueIds, ownerId);

        if (ownedCount != uniqueIds.size()) {
            throw new BadRequestException("Display order payload can contain owned localizations only");
        }
    }

    private Localization getSharedLocalization(Long id) {
        return localizationRepository.findByIdAndOwnerIsNull(id)
                .orElseThrow(() -> new LocalizationNotFoundException(id));
    }

    private Localization getOwnedLocalization(Long ownerId, Long localizationId) {
        return localizationRepository.findByIdAndOwnerId(localizationId, ownerId)
                .orElseThrow(() -> new LocalizationNotFoundException(localizationId));
    }
}
