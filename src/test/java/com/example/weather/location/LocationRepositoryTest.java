package com.example.weather.location;

import com.example.weather.auth.AppUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class LocationRepositoryTest {

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findAllByOwnerIsNullOrderBySortOrderAsc_returnsOnlySharedRows() {
        persistSharedLocation("Warsaw", 2L);
        persistPrivateLocation("Berlin", 99L);
        persistSharedLocation("Gdansk", 1L);

        List<Location> results = locationRepository.findAllByOwnerIsNullOrderBySortOrderAsc();

        assertThat(results)
                .extracting(Location::getName)
                .containsExactly("Gdansk", "Warsaw");
        assertThat(results)
                .allMatch(location -> location.getOwner() == null);
    }

    @Test
    void findByIdAndOwnerIsNull_returnsEmptyForPrivateRow() {
        Location privateLocation = persistPrivateLocation("Berlin", 1L);

        assertThat(locationRepository.findByIdAndOwnerIsNull(privateLocation.getId())).isEmpty();
    }

    @Test
    void findTopByOwnerIsNullOrderBySortOrderDesc_ignoresPrivateRows() {
        persistSharedLocation("Warsaw", 1L);
        persistSharedLocation("Gdansk", 2L);
        persistPrivateLocation("Berlin", 999L);

        Location result = locationRepository.findTopByOwnerIsNullOrderBySortOrderDesc().orElseThrow();

        assertThat(result.getName()).isEqualTo("Gdansk");
        assertThat(result.getSortOrder()).isEqualTo(2L);
    }

    @Test
    void countByIdInAndOwnerIsNull_countsOnlySharedIds() {
        Location shared = persistSharedLocation("Warsaw", 1L);
        Location privateLocation = persistPrivateLocation("Berlin", 2L);

        long count = locationRepository.countByIdInAndOwnerIsNull(List.of(shared.getId(), privateLocation.getId()));

        assertThat(count).isEqualTo(1L);
    }

    @Test
    void findAllByOwnerIdOrderBySortOrderAsc_returnsOnlyOwnedRows() {
        AppUser owner = persistOwner("owner-a");
        AppUser foreignOwner = persistOwner("owner-b");

        entityManager.persistAndFlush(new Location(null, "Warsaw", "Poland", "Region", 21.0, 52.0, 2L, owner));
        entityManager.persistAndFlush(new Location(null, "Berlin", "Germany", "Region", 13.4, 52.5, 1L, foreignOwner));
        entityManager.persistAndFlush(new Location(null, "Gdansk", "Poland", "Region", 18.6, 54.3, 1L, owner));

        List<Location> results = locationRepository.findAllByOwnerIdOrderBySortOrderAsc(owner.getId());

        assertThat(results)
                .extracting(Location::getName)
                .containsExactly("Gdansk", "Warsaw");
        assertThat(results)
                .allMatch(location -> location.getOwner() != null && location.getOwner().getId().equals(owner.getId()));
    }

    @Test
    void findByIdAndOwnerId_returnsEmptyForForeignRow() {
        AppUser owner = persistOwner("owner-a");
        AppUser foreignOwner = persistOwner("owner-b");
        var foreignLocation = entityManager.persistAndFlush(
                new Location(null, "Berlin", "Germany", "Region", 13.4, 52.5, 1L, foreignOwner)
        );

        assertThat(locationRepository.findByIdAndOwnerId(foreignLocation.getId(), owner.getId())).isEmpty();
    }

    @Test
    void findTopByOwnerIdOrderBySortOrderDesc_usesOnlyOwnerScope() {
        AppUser owner = persistOwner("owner-a");
        AppUser foreignOwner = persistOwner("owner-b");

        entityManager.persistAndFlush(new Location(null, "Warsaw", "Poland", "Region", 21.0, 52.0, 1L, owner));
        entityManager.persistAndFlush(new Location(null, "Gdansk", "Poland", "Region", 18.6, 54.3, 2L, owner));
        entityManager.persistAndFlush(new Location(null, "Berlin", "Germany", "Region", 13.4, 52.5, 5L, foreignOwner));

        var result = locationRepository.findTopByOwnerIdOrderBySortOrderDesc(owner.getId()).orElseThrow();

        assertThat(result.getName()).isEqualTo("Gdansk");
        assertThat(result.getSortOrder()).isEqualTo(2L);
    }

    @Test
    void countByIdInAndOwnerId_countsOnlyOwnedIds() {
        AppUser owner = persistOwner("owner-a");
        AppUser foreignOwner = persistOwner("owner-b");
        var owned = entityManager.persistAndFlush(
                new Location(null, "Warsaw", "Poland", "Region", 21.0, 52.0, 1L, owner)
        );
        Location foreignLocation = entityManager.persistAndFlush(
                new Location(null, "Berlin", "Germany", "Region", 13.4, 52.5, 2L, foreignOwner)
        );

        long count = locationRepository.countByIdInAndOwnerId(
                List.of(owned.getId(), foreignLocation.getId()),
                owner.getId()
        );

        assertThat(count).isEqualTo(1L);
    }

    private Location persistSharedLocation(String city, Long sortOrder) {
        var location = new Location(null, city, "Poland", "Region", 21.0, 52.0, sortOrder, null);
        return entityManager.persistAndFlush(location);
    }

    private Location persistPrivateLocation(String city, Long sortOrder) {
        AppUser owner = persistOwner(city.toLowerCase() + "_owner");

        var location = new Location(null, city, "Germany", "Region", 13.4, 52.5, sortOrder, owner);
        return entityManager.persistAndFlush(location);
    }

    private AppUser persistOwner(String username) {
        return entityManager.persistAndFlush(AppUser.builder()
                .username(username)
                .passwordHash("hash")
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build());
    }
}
