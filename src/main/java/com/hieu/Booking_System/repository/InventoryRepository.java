package com.hieu.Booking_System.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hieu.Booking_System.entity.InventoryEntity;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity, Long> {
    Optional<InventoryEntity> findByLocationIdAndVaccineId(Long locationId, Long vaccineId);

    @Query(
            """
		SELECT i.quantity
		FROM InventoryEntity i
		WHERE i.location.id = :locationId
		AND i.vaccine.id = :vaccineId
		AND (i.expiryDate IS NULL OR i.expiryDate > CURRENT_DATE)
		""")
    Integer getAvailableQuantity(@Param("locationId") Long locationId, @Param("vaccineId") Long vaccineId);
}
