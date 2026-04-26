package com.example.weather.location;

import com.example.weather.auth.AppUser;
import com.example.weather.auth.AppUserRepository;
import com.example.weather.location.dto.OrderByDTO;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true"
})
@Import(LocationService.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class LocationReorderPostgresIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private LocationService locationService;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void saveDisplayOrder_reordersSharedRowsWithoutAffectingPrivateRows() {
        locationRepository.deleteAllInBatch();
        appUserRepository.deleteAllInBatch();
        locationRepository.flush();
        appUserRepository.flush();

        Location sharedFirst = locationRepository.save(
                new Location(null, "Warsaw", "Poland", "Mazowieckie", 21.0122, 52.2297, 1L, null)
        );
        Location sharedSecond = locationRepository.save(
                new Location(null, "Gdansk", "Poland", "Pomorskie", 18.6466, 54.3520, 2L, null)
        );

        AppUser owner = appUserRepository.save(AppUser.builder()
                .username("private-owner")
                .passwordHash("hash")
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build());

        Location privateLocation = locationRepository.save(
                new Location(null, "Berlin", "Germany", "Berlin", 13.4050, 52.5200, 1L, owner)
        );

        List<Location> result = locationService.saveDisplayOrder(List.of(
                new OrderByDTO(sharedSecond.getId(), 1L),
                new OrderByDTO(sharedFirst.getId(), 2L)
        ));

        List<Location> persistedSharedRows = locationRepository.findAllByOwnerIsNullOrderBySortOrderAsc();
        Location persistedPrivateRow = locationRepository.findById(privateLocation.getId()).orElseThrow();

        assertThat(result)
                .extracting(Location::getId)
                .containsExactly(sharedSecond.getId(), sharedFirst.getId());

        assertThat(persistedSharedRows)
                .extracting(Location::getId)
                .containsExactly(sharedSecond.getId(), sharedFirst.getId());

        assertThat(persistedSharedRows)
                .extracting(Location::getSortOrder)
                .containsExactly(1L, 2L);

        assertThat(persistedPrivateRow.getOwner()).isNotNull();
        assertThat(persistedPrivateRow.getSortOrder()).isEqualTo(1L);
    }

    @Test
    void deletingUser_removesOwnedPrivateLocationsWithoutAffectingSharedRows() {
        locationRepository.deleteAllInBatch();
        appUserRepository.deleteAllInBatch();
        locationRepository.flush();
        appUserRepository.flush();

        var sharedLocation = locationRepository.save(
                new Location(null, "Warsaw", "Poland", "Mazowieckie", 21.0122, 52.2297, 1L, null)
        );

        AppUser owner = appUserRepository.save(AppUser.builder()
                .username("owned-locations-user")
                .passwordHash("hash")
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build());

        var privateLocation = locationRepository.save(
                new Location(null, "Berlin", "Germany", "Berlin", 13.4050, 52.5200, 1L, owner)
        );

        appUserRepository.delete(owner);
        appUserRepository.flush();
        entityManager.clear();

        assertThat(locationRepository.findById(privateLocation.getId())).isEmpty();
        assertThat(locationRepository.findById(sharedLocation.getId())).isPresent();
        assertThat(locationRepository.findAllByOwnerIsNullOrderBySortOrderAsc())
                .extracting(Location::getId)
                .containsExactly(sharedLocation.getId());
    }
}
