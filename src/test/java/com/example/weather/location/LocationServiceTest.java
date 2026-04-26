package com.example.weather.location;
import com.example.weather.auth.AppUser;
import com.example.weather.auth.AppUserRepository;
import com.example.weather.common.BadRequestException;
import com.example.weather.common.LocationNotFoundException;
import com.example.weather.location.dto.OrderByDTO;
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
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private LocationService locationService;

    @Test
    void getSharedLocations_returnsOnlySharedRows() {
        List<Location> sharedRows = List.of(
                new Location(1L, "Warsaw", "Poland", "Mazowieckie", 21.0122, 52.2297, 1L, null)
        );
        when(locationRepository.findAllByOwnerIsNullOrderBySortOrderAsc()).thenReturn(sharedRows);

        List<Location> results = locationService.getSharedLocations();

        assertThat(results).isEqualTo(sharedRows);
        verify(locationRepository).findAllByOwnerIsNullOrderBySortOrderAsc();
    }

    @Test
    void createSharedLocation_createsSharedRowWithNullOwner() {
        when(locationRepository.findTopByOwnerIsNullOrderBySortOrderDesc())
                .thenReturn(Optional.of(new Location(1L, "Warsaw", "Poland", "Mazowieckie", 21.0122, 52.2297, 2L, null)));
        when(locationRepository.save(any(Location.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = locationService.createSharedLocation("Gdansk", 18.6466, 54.3520, "Pomorskie", "Poland");

        ArgumentCaptor<Location> captor = ArgumentCaptor.forClass(Location.class);
        verify(locationRepository).save(captor.capture());

        Location saved = captor.getValue();
        assertThat(saved.getOwner()).isNull();
        assertThat(saved.getSortOrder()).isEqualTo(3L);
        assertThat(result.getOwner()).isNull();
    }

    @Test
    void updateLocation_updatesSharedRow() {
        var shared = new Location(1L, "Warsaw", "Poland", "Mazowieckie", 21.0122, 52.2297, 1L, null);
        when(locationRepository.findByIdAndOwnerIsNull(1L)).thenReturn(Optional.of(shared));
        when(locationRepository.save(shared)).thenReturn(shared);

        var result = locationService.updateLocation(1L, "Gdansk", 18.6466, 54.3520, "Pomorskie", "Poland");

        assertThat(result.getName()).isEqualTo("Gdansk");
        assertThat(result.getRegion()).isEqualTo("Pomorskie");
    }

    @Test
    void updateLocation_throwsNotFoundForPrivateRow() {
        when(locationRepository.findByIdAndOwnerIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationService.updateLocation(99L, "Berlin", 13.4050, 52.5200, "Berlin", "Germany"))
                .isInstanceOf(LocationNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteLocation_deletesSharedRow() {
        var shared = new Location(1L, "Warsaw", "Poland", "Mazowieckie", 21.0122, 52.2297, 1L, null);
        when(locationRepository.findByIdAndOwnerIsNull(1L)).thenReturn(Optional.of(shared));

        locationService.deleteLocation(1L);

        verify(locationRepository).delete(shared);
    }

    @Test
    void deleteLocation_throwsNotFoundForPrivateRow() {
        when(locationRepository.findByIdAndOwnerIsNull(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationService.deleteLocation(7L))
                .isInstanceOf(LocationNotFoundException.class)
                .hasMessageContaining("7");
        verify(locationRepository, never()).delete(any());
    }

    @Test
    void saveDisplayOrder_reordersSharedRows() {
        List<OrderByDTO> orders = List.of(
                new OrderByDTO(1L, 1L),
                new OrderByDTO(2L, 2L)
        );
        List<Location> reordered = List.of(
                new Location(1L, "Warsaw", "Poland", "Mazowieckie", 21.0122, 52.2297, 1L, null),
                new Location(2L, "Gdansk", "Poland", "Pomorskie", 18.6466, 54.3520, 2L, null)
        );

        when(locationRepository.countByIdInAndOwnerIsNull(anyCollection())).thenReturn(2L);
        when(locationRepository.findAllByOwnerIsNullOrderBySortOrderAsc()).thenReturn(reordered);

        List<Location> result = locationService.saveDisplayOrder(orders);

        assertThat(result).isEqualTo(reordered);
        verify(locationRepository, times(4)).updateSortOrder(anyLong(), anyLong());
        verify(entityManager).flush();
        verify(entityManager).clear();
    }

    @Test
    void saveDisplayOrder_rejectsPayloadContainingPrivateId() {
        List<OrderByDTO> orders = List.of(
                new OrderByDTO(1L, 1L),
                new OrderByDTO(2L, 2L)
        );
        when(locationRepository.countByIdInAndOwnerIsNull(anyCollection())).thenReturn(1L);

        assertThatThrownBy(() -> locationService.saveDisplayOrder(orders))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("shared locations only");

        verify(locationRepository, never()).updateSortOrder(anyLong(), anyLong());
    }

    @Test
    void getPrivateLocations_returnsOnlyCurrentUserRows() {
        List<Location> ownedRows = List.of(
                new Location(1L, "Berlin", "Germany", "Berlin", 13.4050, 52.5200, 1L, buildOwner(10L, "owner"))
        );
        when(locationRepository.findAllByOwnerIdOrderBySortOrderAsc(10L)).thenReturn(ownedRows);

        List<Location> results = locationService.getPrivateLocations(10L);

        assertThat(results).isEqualTo(ownedRows);
        verify(locationRepository).findAllByOwnerIdOrderBySortOrderAsc(10L);
    }

    @Test
    void createPrivateLocation_setsOwnerAndOwnerScopedSortOrder() {
        var owner = buildOwner(10L, "owner");
        when(locationRepository.findTopByOwnerIdOrderBySortOrderDesc(10L))
                .thenReturn(Optional.of(new Location(1L, "Berlin", "Germany", "Berlin", 13.4050, 52.5200, 2L, owner)));
        when(appUserRepository.findById(10L)).thenReturn(Optional.of(owner));
        when(locationRepository.save(any(Location.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = locationService.createPrivateLocation(10L, "Hamburg", 9.9937, 53.5511, "Hamburg", "Germany");

        ArgumentCaptor<Location> captor = ArgumentCaptor.forClass(Location.class);
        verify(locationRepository).save(captor.capture());

        var saved = captor.getValue();
        assertThat(saved.getOwner()).isEqualTo(owner);
        assertThat(saved.getSortOrder()).isEqualTo(3L);
        assertThat(result.getOwner()).isEqualTo(owner);
    }

    @Test
    void updatePrivateLocation_updatesOwnedRow() {
        var owner = buildOwner(10L, "owner");
        var owned = new Location(1L, "Berlin", "Germany", "Berlin", 13.4050, 52.5200, 1L, owner);
        when(locationRepository.findByIdAndOwnerId(1L, 10L)).thenReturn(Optional.of(owned));
        when(locationRepository.save(owned)).thenReturn(owned);

        var result = locationService.updatePrivateLocation(10L, 1L, "Hamburg", 9.9937, 53.5511, "Hamburg", "Germany");

        assertThat(result.getName()).isEqualTo("Hamburg");
        assertThat(result.getRegion()).isEqualTo("Hamburg");
    }

    @Test
    void updatePrivateLocation_throwsNotFoundForForeignRow() {
        when(locationRepository.findByIdAndOwnerId(99L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationService.updatePrivateLocation(10L, 99L, "Berlin", 13.4050, 52.5200, "Berlin", "Germany"))
                .isInstanceOf(LocationNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deletePrivateLocation_deletesOwnedRow() {
        var owner = buildOwner(10L, "owner");
        var owned = new Location(1L, "Berlin", "Germany", "Berlin", 13.4050, 52.5200, 1L, owner);
        when(locationRepository.findByIdAndOwnerId(1L, 10L)).thenReturn(Optional.of(owned));

        locationService.deletePrivateLocation(10L, 1L);

        verify(locationRepository).delete(owned);
    }

    @Test
    void deletePrivateLocation_throwsNotFoundForForeignRow() {
        when(locationRepository.findByIdAndOwnerId(7L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationService.deletePrivateLocation(10L, 7L))
                .isInstanceOf(LocationNotFoundException.class)
                .hasMessageContaining("7");
        verify(locationRepository, never()).delete(any());
    }

    @Test
    void savePrivateDisplayOrder_reordersOwnedRows() {
        List<OrderByDTO> orders = List.of(
                new OrderByDTO(1L, 1L),
                new OrderByDTO(2L, 2L)
        );
        AppUser owner = buildOwner(10L, "owner");
        List<Location> reordered = List.of(
                new Location(1L, "Berlin", "Germany", "Berlin", 13.4050, 52.5200, 1L, owner),
                new Location(2L, "Hamburg", "Germany", "Hamburg", 9.9937, 53.5511, 2L, owner)
        );

        when(locationRepository.countByIdInAndOwnerId(anyCollection(), anyLong())).thenReturn(2L);
        when(locationRepository.findAllByOwnerIdOrderBySortOrderAsc(10L)).thenReturn(reordered);

        List<Location> result = locationService.savePrivateDisplayOrder(10L, orders);

        assertThat(result).isEqualTo(reordered);
        verify(locationRepository, times(4)).updateSortOrder(anyLong(), anyLong());
        verify(entityManager).flush();
        verify(entityManager).clear();
    }

    @Test
    void savePrivateDisplayOrder_rejectsPayloadWithForeignId() {
        List<OrderByDTO> orders = List.of(
                new OrderByDTO(1L, 1L),
                new OrderByDTO(2L, 2L)
        );
        when(locationRepository.countByIdInAndOwnerId(anyCollection(), anyLong())).thenReturn(1L);

        assertThatThrownBy(() -> locationService.savePrivateDisplayOrder(10L, orders))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("owned locations only");

        verify(locationRepository, never()).updateSortOrder(anyLong(), anyLong());
    }

    private AppUser buildOwner(Long id, String username) {
        var owner = new AppUser();
        owner.setId(id);
        owner.setUsername(username);
        owner.setPasswordHash("hash");
        owner.setEnabled(true);
        return owner;
    }
}
