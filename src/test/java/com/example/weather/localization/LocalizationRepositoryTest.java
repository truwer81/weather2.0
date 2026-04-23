package com.example.weather.localization;

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
class LocalizationRepositoryTest {

    @Autowired
    private LocalizationRepository localizationRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findAllByOwnerIsNullOrderBySortOrderAsc_returnsOnlySharedRows() {
        persistSharedLocalization("Warsaw", 2L);
        persistPrivateLocalization("Berlin", 99L);
        persistSharedLocalization("Gdansk", 1L);

        List<Localization> results = localizationRepository.findAllByOwnerIsNullOrderBySortOrderAsc();

        assertThat(results)
                .extracting(Localization::getCity)
                .containsExactly("Gdansk", "Warsaw");
        assertThat(results)
                .allMatch(localization -> localization.getOwner() == null);
    }

    @Test
    void findByIdAndOwnerIsNull_returnsEmptyForPrivateRow() {
        Localization privateLocalization = persistPrivateLocalization("Berlin", 1L);

        assertThat(localizationRepository.findByIdAndOwnerIsNull(privateLocalization.getId())).isEmpty();
    }

    @Test
    void findTopByOwnerIsNullOrderBySortOrderDesc_ignoresPrivateRows() {
        persistSharedLocalization("Warsaw", 1L);
        persistSharedLocalization("Gdansk", 2L);
        persistPrivateLocalization("Berlin", 999L);

        Localization result = localizationRepository.findTopByOwnerIsNullOrderBySortOrderDesc().orElseThrow();

        assertThat(result.getCity()).isEqualTo("Gdansk");
        assertThat(result.getSortOrder()).isEqualTo(2L);
    }

    @Test
    void countByIdInAndOwnerIsNull_countsOnlySharedIds() {
        Localization shared = persistSharedLocalization("Warsaw", 1L);
        Localization privateLocalization = persistPrivateLocalization("Berlin", 2L);

        long count = localizationRepository.countByIdInAndOwnerIsNull(List.of(shared.getId(), privateLocalization.getId()));

        assertThat(count).isEqualTo(1L);
    }

    @Test
    void findAllByOwnerIdOrderBySortOrderAsc_returnsOnlyOwnedRows() {
        AppUser owner = persistOwner("owner-a");
        AppUser foreignOwner = persistOwner("owner-b");

        entityManager.persistAndFlush(new Localization(null, "Warsaw", "Poland", "Region", 21.0, 52.0, 2L, owner));
        entityManager.persistAndFlush(new Localization(null, "Berlin", "Germany", "Region", 13.4, 52.5, 1L, foreignOwner));
        entityManager.persistAndFlush(new Localization(null, "Gdansk", "Poland", "Region", 18.6, 54.3, 1L, owner));

        List<Localization> results = localizationRepository.findAllByOwnerIdOrderBySortOrderAsc(owner.getId());

        assertThat(results)
                .extracting(Localization::getCity)
                .containsExactly("Gdansk", "Warsaw");
        assertThat(results)
                .allMatch(localization -> localization.getOwner() != null && localization.getOwner().getId().equals(owner.getId()));
    }

    @Test
    void findByIdAndOwnerId_returnsEmptyForForeignRow() {
        AppUser owner = persistOwner("owner-a");
        AppUser foreignOwner = persistOwner("owner-b");
        Localization foreignLocalization = entityManager.persistAndFlush(
                new Localization(null, "Berlin", "Germany", "Region", 13.4, 52.5, 1L, foreignOwner)
        );

        assertThat(localizationRepository.findByIdAndOwnerId(foreignLocalization.getId(), owner.getId())).isEmpty();
    }

    @Test
    void findTopByOwnerIdOrderBySortOrderDesc_usesOnlyOwnerScope() {
        AppUser owner = persistOwner("owner-a");
        AppUser foreignOwner = persistOwner("owner-b");

        entityManager.persistAndFlush(new Localization(null, "Warsaw", "Poland", "Region", 21.0, 52.0, 1L, owner));
        entityManager.persistAndFlush(new Localization(null, "Gdansk", "Poland", "Region", 18.6, 54.3, 2L, owner));
        entityManager.persistAndFlush(new Localization(null, "Berlin", "Germany", "Region", 13.4, 52.5, 5L, foreignOwner));

        Localization result = localizationRepository.findTopByOwnerIdOrderBySortOrderDesc(owner.getId()).orElseThrow();

        assertThat(result.getCity()).isEqualTo("Gdansk");
        assertThat(result.getSortOrder()).isEqualTo(2L);
    }

    @Test
    void countByIdInAndOwnerId_countsOnlyOwnedIds() {
        AppUser owner = persistOwner("owner-a");
        AppUser foreignOwner = persistOwner("owner-b");
        Localization owned = entityManager.persistAndFlush(
                new Localization(null, "Warsaw", "Poland", "Region", 21.0, 52.0, 1L, owner)
        );
        Localization foreignLocalization = entityManager.persistAndFlush(
                new Localization(null, "Berlin", "Germany", "Region", 13.4, 52.5, 2L, foreignOwner)
        );

        long count = localizationRepository.countByIdInAndOwnerId(
                List.of(owned.getId(), foreignLocalization.getId()),
                owner.getId()
        );

        assertThat(count).isEqualTo(1L);
    }

    private Localization persistSharedLocalization(String city, Long sortOrder) {
        Localization localization = new Localization(null, city, "Poland", "Region", 21.0, 52.0, sortOrder, null);
        return entityManager.persistAndFlush(localization);
    }

    private Localization persistPrivateLocalization(String city, Long sortOrder) {
        AppUser owner = persistOwner(city.toLowerCase() + "_owner");

        Localization localization = new Localization(null, city, "Germany", "Region", 13.4, 52.5, sortOrder, owner);
        return entityManager.persistAndFlush(localization);
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
