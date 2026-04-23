package com.example.weather.localization;
import com.example.weather.auth.AppUser;
import com.example.weather.auth.AppUserRepository;
import com.example.weather.common.BadRequestException;
import com.example.weather.common.LocalizationNotFoundException;
import com.example.weather.localization.dto.OrderByDTO;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalizationServiceTest {

    @Mock
    private LocalizationRepository localizationRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private LocalizationService localizationService;

    @Test
    void getAllLocalizations_returnsOnlySharedRows() {
        List<Localization> sharedRows = List.of(
                new Localization(1L, "Warsaw", "Poland", "Mazowieckie", 21.0122, 52.2297, 1L, null)
        );
        when(localizationRepository.findAllByOwnerIsNullOrderBySortOrderAsc()).thenReturn(sharedRows);

        List<Localization> results = localizationService.getAllLocalizations();

        assertThat(results).isEqualTo(sharedRows);
        verify(localizationRepository).findAllByOwnerIsNullOrderBySortOrderAsc();
    }

    @Test
    void createLocalization_createsSharedRowWithNullOwner() {
        when(localizationRepository.findTopByOwnerIsNullOrderBySortOrderDesc())
                .thenReturn(Optional.of(new Localization(1L, "Warsaw", "Poland", "Mazowieckie", 21.0122, 52.2297, 2L, null)));
        when(localizationRepository.save(any(Localization.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Localization result = localizationService.createLocalization("Gdansk", 18.6466, 54.3520, "Pomorskie", "Poland");

        ArgumentCaptor<Localization> captor = ArgumentCaptor.forClass(Localization.class);
        verify(localizationRepository).save(captor.capture());

        Localization saved = captor.getValue();
        assertThat(saved.getOwner()).isNull();
        assertThat(saved.getSortOrder()).isEqualTo(3L);
        assertThat(result.getOwner()).isNull();
    }

    @Test
    void updateLocalization_updatesSharedRow() {
        Localization shared = new Localization(1L, "Warsaw", "Poland", "Mazowieckie", 21.0122, 52.2297, 1L, null);
        when(localizationRepository.findByIdAndOwnerIsNull(1L)).thenReturn(Optional.of(shared));
        when(localizationRepository.save(shared)).thenReturn(shared);

        Localization result = localizationService.updateLocalization(1L, "Gdansk", 18.6466, 54.3520, "Pomorskie", "Poland");

        assertThat(result.getCity()).isEqualTo("Gdansk");
        assertThat(result.getRegion()).isEqualTo("Pomorskie");
    }

    @Test
    void updateLocalization_throwsNotFoundForPrivateRow() {
        when(localizationRepository.findByIdAndOwnerIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> localizationService.updateLocalization(99L, "Berlin", 13.4050, 52.5200, "Berlin", "Germany"))
                .isInstanceOf(LocalizationNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteLocalization_deletesSharedRow() {
        Localization shared = new Localization(1L, "Warsaw", "Poland", "Mazowieckie", 21.0122, 52.2297, 1L, null);
        when(localizationRepository.findByIdAndOwnerIsNull(1L)).thenReturn(Optional.of(shared));

        localizationService.deleteLocalization(1L);

        verify(localizationRepository).delete(shared);
    }

    @Test
    void deleteLocalization_throwsNotFoundForPrivateRow() {
        when(localizationRepository.findByIdAndOwnerIsNull(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> localizationService.deleteLocalization(7L))
                .isInstanceOf(LocalizationNotFoundException.class)
                .hasMessageContaining("7");
        verify(localizationRepository, never()).delete(any());
    }

    @Test
    void saveDisplayOrder_reordersSharedRows() {
        List<OrderByDTO> orders = List.of(
                new OrderByDTO(1L, 1L),
                new OrderByDTO(2L, 2L)
        );
        List<Localization> reordered = List.of(
                new Localization(1L, "Warsaw", "Poland", "Mazowieckie", 21.0122, 52.2297, 1L, null),
                new Localization(2L, "Gdansk", "Poland", "Pomorskie", 18.6466, 54.3520, 2L, null)
        );

        when(localizationRepository.countByIdInAndOwnerIsNull(anyCollection())).thenReturn(2L);
        when(localizationRepository.findAllByOwnerIsNullOrderBySortOrderAsc()).thenReturn(reordered);

        List<Localization> result = localizationService.saveDisplayOrder(orders);

        assertThat(result).isEqualTo(reordered);
        verify(localizationRepository, times(4)).updateSortOrder(anyLong(), anyLong());
        verify(entityManager).flush();
        verify(entityManager).clear();
    }

    @Test
    void saveDisplayOrder_rejectsPayloadContainingPrivateId() {
        List<OrderByDTO> orders = List.of(
                new OrderByDTO(1L, 1L),
                new OrderByDTO(2L, 2L)
        );
        when(localizationRepository.countByIdInAndOwnerIsNull(anyCollection())).thenReturn(1L);

        assertThatThrownBy(() -> localizationService.saveDisplayOrder(orders))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("shared localizations only");

        verify(localizationRepository, never()).updateSortOrder(anyLong(), anyLong());
    }

    @Test
    void getPrivateLocalizations_returnsOnlyCurrentUserRows() {
        List<Localization> ownedRows = List.of(
                new Localization(1L, "Berlin", "Germany", "Berlin", 13.4050, 52.5200, 1L, buildOwner(10L, "owner"))
        );
        when(localizationRepository.findAllByOwnerIdOrderBySortOrderAsc(10L)).thenReturn(ownedRows);

        List<Localization> results = localizationService.getPrivateLocalizations(10L);

        assertThat(results).isEqualTo(ownedRows);
        verify(localizationRepository).findAllByOwnerIdOrderBySortOrderAsc(10L);
    }

    @Test
    void createPrivateLocalization_setsOwnerAndOwnerScopedSortOrder() {
        AppUser owner = buildOwner(10L, "owner");
        when(localizationRepository.findTopByOwnerIdOrderBySortOrderDesc(10L))
                .thenReturn(Optional.of(new Localization(1L, "Berlin", "Germany", "Berlin", 13.4050, 52.5200, 2L, owner)));
        when(appUserRepository.findById(10L)).thenReturn(Optional.of(owner));
        when(localizationRepository.save(any(Localization.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Localization result = localizationService.createPrivateLocalization(10L, "Hamburg", 9.9937, 53.5511, "Hamburg", "Germany");

        ArgumentCaptor<Localization> captor = ArgumentCaptor.forClass(Localization.class);
        verify(localizationRepository).save(captor.capture());

        Localization saved = captor.getValue();
        assertThat(saved.getOwner()).isEqualTo(owner);
        assertThat(saved.getSortOrder()).isEqualTo(3L);
        assertThat(result.getOwner()).isEqualTo(owner);
    }

    @Test
    void updatePrivateLocalization_updatesOwnedRow() {
        AppUser owner = buildOwner(10L, "owner");
        Localization owned = new Localization(1L, "Berlin", "Germany", "Berlin", 13.4050, 52.5200, 1L, owner);
        when(localizationRepository.findByIdAndOwnerId(1L, 10L)).thenReturn(Optional.of(owned));
        when(localizationRepository.save(owned)).thenReturn(owned);

        Localization result = localizationService.updatePrivateLocalization(10L, 1L, "Hamburg", 9.9937, 53.5511, "Hamburg", "Germany");

        assertThat(result.getCity()).isEqualTo("Hamburg");
        assertThat(result.getRegion()).isEqualTo("Hamburg");
    }

    @Test
    void updatePrivateLocalization_throwsNotFoundForForeignRow() {
        when(localizationRepository.findByIdAndOwnerId(99L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> localizationService.updatePrivateLocalization(10L, 99L, "Berlin", 13.4050, 52.5200, "Berlin", "Germany"))
                .isInstanceOf(LocalizationNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deletePrivateLocalization_deletesOwnedRow() {
        AppUser owner = buildOwner(10L, "owner");
        Localization owned = new Localization(1L, "Berlin", "Germany", "Berlin", 13.4050, 52.5200, 1L, owner);
        when(localizationRepository.findByIdAndOwnerId(1L, 10L)).thenReturn(Optional.of(owned));

        localizationService.deletePrivateLocalization(10L, 1L);

        verify(localizationRepository).delete(owned);
    }

    @Test
    void deletePrivateLocalization_throwsNotFoundForForeignRow() {
        when(localizationRepository.findByIdAndOwnerId(7L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> localizationService.deletePrivateLocalization(10L, 7L))
                .isInstanceOf(LocalizationNotFoundException.class)
                .hasMessageContaining("7");
        verify(localizationRepository, never()).delete(any());
    }

    @Test
    void savePrivateDisplayOrder_reordersOwnedRows() {
        List<OrderByDTO> orders = List.of(
                new OrderByDTO(1L, 1L),
                new OrderByDTO(2L, 2L)
        );
        AppUser owner = buildOwner(10L, "owner");
        List<Localization> reordered = List.of(
                new Localization(1L, "Berlin", "Germany", "Berlin", 13.4050, 52.5200, 1L, owner),
                new Localization(2L, "Hamburg", "Germany", "Hamburg", 9.9937, 53.5511, 2L, owner)
        );

        when(localizationRepository.countByIdInAndOwnerId(anyCollection(), anyLong())).thenReturn(2L);
        when(localizationRepository.findAllByOwnerIdOrderBySortOrderAsc(10L)).thenReturn(reordered);

        List<Localization> result = localizationService.savePrivateDisplayOrder(10L, orders);

        assertThat(result).isEqualTo(reordered);
        verify(localizationRepository, times(4)).updateSortOrder(anyLong(), anyLong());
        verify(entityManager).flush();
        verify(entityManager).clear();
    }

    @Test
    void savePrivateDisplayOrder_rejectsPayloadWithForeignId() {
        List<OrderByDTO> orders = List.of(
                new OrderByDTO(1L, 1L),
                new OrderByDTO(2L, 2L)
        );
        when(localizationRepository.countByIdInAndOwnerId(anyCollection(), anyLong())).thenReturn(1L);

        assertThatThrownBy(() -> localizationService.savePrivateDisplayOrder(10L, orders))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("owned localizations only");

        verify(localizationRepository, never()).updateSortOrder(anyLong(), anyLong());
    }

    private AppUser buildOwner(Long id, String username) {
        AppUser owner = new AppUser();
        owner.setId(id);
        owner.setUsername(username);
        owner.setPasswordHash("hash");
        owner.setEnabled(true);
        return owner;
    }
}
