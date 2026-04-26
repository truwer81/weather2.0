package com.example.weather.location;

import com.example.weather.auth.AppUser;
import com.example.weather.auth.AppUserRepository;
import com.example.weather.common.BadRequestException;
import com.example.weather.common.LocationNotFoundException;
import com.example.weather.location.dto.OrderByDTO;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final AppUserRepository appUserRepository;
    private final EntityManager entityManager;

    @Transactional
    public Location createSharedLocation(String city, Double longitude, Double latitude, String region, String country) {
        validateCoordinates(longitude, latitude);
        validateRequiredFields(city, country);

        if (region != null && region.isBlank()) {
            region = null;
        }

        long topSortOrder = locationRepository.findTopByOwnerIsNullOrderBySortOrderDesc()
                .map(Location::getSortOrder)
                .map(sortOrder -> sortOrder + 1)
                .orElse(0L);

        var location = new Location(null, city, country, region, longitude, latitude, topSortOrder, null);
        return locationRepository.save(location);
    }

    @Transactional(readOnly = true)
    public List<Location> getSharedLocations() {
        return locationRepository.findAllByOwnerIsNullOrderBySortOrderAsc();
    }

    @Transactional(readOnly = true)
    public List<Location> getPrivateLocations(Long ownerId) {
        return locationRepository.findAllByOwnerIdOrderBySortOrderAsc(ownerId);
    }

    @Transactional(readOnly = true)
    public Location getSharedLocation(Long id) {
        return locationRepository.findByIdAndOwnerIsNull(id)
                .orElseThrow(() -> new LocationNotFoundException(id));
    }

    @Transactional
    public void deleteLocation(long locationId) {
        var location = getSharedLocation(locationId);

        locationRepository.delete(location);
    }

    @Transactional
    public void deletePrivateLocation(long ownerId, long locationId) {
        var location = getOwnedLocation(ownerId, locationId);

        locationRepository.delete(location);
    }

    @Transactional
    public Location updateLocation(
            Long locationId,
            String name,
            Double longitude,
            Double latitude,
            String region,
            String country
    ) {
        validateCoordinates(longitude, latitude);
        validateRequiredFields(name, country);

        var location = getSharedLocation(locationId);

        location.setName(name.trim());
        location.setCountry(country.trim());
        location.setRegion(region == null || region.isBlank() ? null : region.trim());
        location.setLongitude(longitude);
        location.setLatitude(latitude);

        return locationRepository.save(location);
    }

    @Transactional
    public Location createPrivateLocation(
            Long ownerId,
            String name,
            Double longitude,
            Double latitude,
            String region,
            String country
    ) {
        validateCoordinates(longitude, latitude);
        validateRequiredFields(name, country);

        if (region != null && region.isBlank()) {
            region = null;
        }

        long topSortOrder = locationRepository.findTopByOwnerIdOrderBySortOrderDesc(ownerId)
                .map(Location::getSortOrder)
                .map(sortOrder -> sortOrder + 1)
                .orElse(0L);

        AppUser owner = appUserRepository.findById(ownerId)
                .orElseThrow(() -> new LocationNotFoundException(ownerId));

        var location = new Location(null, name, country, region, longitude, latitude, topSortOrder, owner);
        return locationRepository.save(location);
    }

    @Transactional
    public Location updatePrivateLocation(
            long ownerId,
            long locationId,
            String city,
            Double longitude,
            Double latitude,
            String region,
            String country
    ) {
        validateCoordinates(longitude, latitude);
        validateRequiredFields(city, country);

        var location = getOwnedLocation(ownerId, locationId);

        location.setName(city.trim());
        location.setCountry(country.trim());
        location.setRegion(region == null || region.isBlank() ? null : region.trim());
        location.setLongitude(longitude);
        location.setLatitude(latitude);

        return locationRepository.save(location);
    }


    @Transactional
    public List<Location> saveDisplayOrder(List<OrderByDTO> orders) {
        validateOrders(orders);
        validateSharedOrderScope(orders);

        for (OrderByDTO item : orders) {
            locationRepository.updateSortOrder(item.locationId(), -item.sortOrder() - 1000);
        }

        for (OrderByDTO item : orders) {
            locationRepository.updateSortOrder(item.locationId(), item.sortOrder());
        }

        entityManager.flush();
        entityManager.clear();

        return locationRepository.findAllByOwnerIsNullOrderBySortOrderAsc();
    }

    @Transactional
    public List<Location> savePrivateDisplayOrder(Long ownerId, List<OrderByDTO> orders) {
        validateOrders(orders);
        validatePrivateOrderScope(ownerId, orders);

        for (OrderByDTO item : orders) {
            locationRepository.updateSortOrder(item.locationId(), -item.sortOrder() - 1000);
        }

        for (OrderByDTO item : orders) {
            locationRepository.updateSortOrder(item.locationId(), item.sortOrder());
        }

        entityManager.flush();
        entityManager.clear();

        return locationRepository.findAllByOwnerIdOrderBySortOrderAsc(ownerId);
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
                .map(OrderByDTO::locationId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        if (uniqueIds.contains(null)) {
            throw new BadRequestException("Location id is required for display order");
        }

        long sharedCount = locationRepository.countByIdInAndOwnerIsNull(uniqueIds);

        if (sharedCount != uniqueIds.size()) {
            throw new BadRequestException("Display order payload can contain shared locations only");
        }
    }

    private void validatePrivateOrderScope(Long ownerId, List<OrderByDTO> orders) {
        Set<Long> uniqueIds = orders.stream()
                .map(OrderByDTO::locationId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        if (uniqueIds.contains(null)) {
            throw new BadRequestException("Location id is required for display order");
        }

        long ownedCount = locationRepository.countByIdInAndOwnerId(uniqueIds, ownerId);

        if (ownedCount != uniqueIds.size()) {
            throw new BadRequestException("Display order payload can contain owned locations only");
        }
    }

    private Location getOwnedLocation(Long ownerId, Long locationId) {
        return locationRepository.findByIdAndOwnerId(locationId, ownerId)
                .orElseThrow(() -> new LocationNotFoundException(locationId));
    }
}
