package com.example.weather.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {

    Optional<Location> findTopByOrderBySortOrderDesc();

    List<Location> findAllByOwnerIsNullOrderBySortOrderAsc();

    Optional<Location> findByIdAndOwnerIsNull(Long id);

    Optional<Location> findTopByOwnerIsNullOrderBySortOrderDesc();

    List<Location> findAllByOwnerIdOrderBySortOrderAsc(Long ownerId);

    Optional<Location> findByIdAndOwnerId(Long id, Long ownerId);

    Optional<Location> findTopByOwnerIdOrderBySortOrderDesc(Long ownerId);

    boolean existsByIdAndOwnerIsNull(Long id);

    long countByIdInAndOwnerIsNull(Collection<Long> ids);

    long countByIdInAndOwnerId(Collection<Long> ids, Long ownerId);

    @Modifying
    @Query("""
        update Location e
        set e.sortOrder = :sortOrder
        where e.id = :id
    """)
    void updateSortOrder(@Param("id") Long id, @Param("sortOrder") Long sortOrder);
}
