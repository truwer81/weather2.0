package com.example.weather.localization;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LocalizationRepository extends JpaRepository<Localization, Long> {

    Optional<Localization> findTopByOrderBySortOrderDesc();

    @Modifying
    @Query("""
    update Localization e
    set e.sortOrder = :sortOrder
    where e.id = :id
""")
    void updateOrderBy(@Param("id") Long id, @Param("sortOrder") Long sortOrder);
}
