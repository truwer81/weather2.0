package com.example.weather.localization;
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
}
