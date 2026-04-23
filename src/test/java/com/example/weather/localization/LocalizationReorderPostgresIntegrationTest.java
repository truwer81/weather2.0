package com.example.weather.localization;

import com.example.weather.auth.AppUser;
import com.example.weather.localization.dto.OrderByDTO;
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
@Import(LocalizationService.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class LocalizationReorderPostgresIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private LocalizationRepository localizationRepository;

    @Autowired
    private LocalizationService localizationService;

    @Autowired
    private com.example.weather.auth.AppUserRepository appUserRepository;

    @Test
    void saveDisplayOrder_reordersSharedRowsWithoutAffectingPrivateRows() {
        localizationRepository.deleteAllInBatch();
        appUserRepository.deleteAllInBatch();
        localizationRepository.flush();
        appUserRepository.flush();

        Localization sharedFirst = localizationRepository.save(
                new Localization(null, "Warsaw", "Poland", "Mazowieckie", 21.0122, 52.2297, 1L, null)
        );
        Localization sharedSecond = localizationRepository.save(
                new Localization(null, "Gdansk", "Poland", "Pomorskie", 18.6466, 54.3520, 2L, null)
        );

        AppUser owner = appUserRepository.save(AppUser.builder()
                .username("private-owner")
                .passwordHash("hash")
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build());

        Localization privateLocalization = localizationRepository.save(
                new Localization(null, "Berlin", "Germany", "Berlin", 13.4050, 52.5200, 1L, owner)
        );

        List<Localization> result = localizationService.saveDisplayOrder(List.of(
                new OrderByDTO(sharedSecond.getId(), 1L),
                new OrderByDTO(sharedFirst.getId(), 2L)
        ));

        List<Localization> persistedSharedRows = localizationRepository.findAllByOwnerIsNullOrderBySortOrderAsc();
        Localization persistedPrivateRow = localizationRepository.findById(privateLocalization.getId()).orElseThrow();

        assertThat(result)
                .extracting(Localization::getId)
                .containsExactly(sharedSecond.getId(), sharedFirst.getId());

        assertThat(persistedSharedRows)
                .extracting(Localization::getId)
                .containsExactly(sharedSecond.getId(), sharedFirst.getId());

        assertThat(persistedSharedRows)
                .extracting(Localization::getSortOrder)
                .containsExactly(1L, 2L);

        assertThat(persistedPrivateRow.getOwner()).isNotNull();
        assertThat(persistedPrivateRow.getSortOrder()).isEqualTo(1L);
    }
}
