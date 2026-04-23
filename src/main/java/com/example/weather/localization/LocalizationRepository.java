package com.example.weather.localization;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LocalizationRepository extends JpaRepository<Localization, Long> {

    Optional<Localization> findTopByOrderBySortOrderDesc();

    List<Localization> findAllByOwnerIsNullOrderBySortOrderAsc();

    Optional<Localization> findByIdAndOwnerIsNull(Long id);

    Optional<Localization> findTopByOwnerIsNullOrderBySortOrderDesc();

    boolean existsByIdAndOwnerIsNull(Long id);

    long countByIdInAndOwnerIsNull(Collection<Long> ids);

    @Modifying
    @Query("""
        update Localization e
        set e.sortOrder = :sortOrder
        where e.id = :id
    """)
    void updateSortOrder(@Param("id") Long id, @Param("sortOrder") Long sortOrder);
}
