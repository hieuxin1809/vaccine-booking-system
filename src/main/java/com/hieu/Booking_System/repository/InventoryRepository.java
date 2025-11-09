package com.hieu.Booking_System.repository;

import com.hieu.Booking_System.entity.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity , Long> {
    Optional<InventoryEntity> findByLocationIdAndVaccineId(Long locationId, Long vaccineId);
    @Query("""
        SELECT i.quantity
        FROM InventoryEntity i
        WHERE i.location.id = :locationId
          AND i.vaccine.id = :vaccineId
          AND (i.expiryDate IS NULL OR i.expiryDate > CURRENT_DATE)
        """)
    Integer getAvailableQuantity(@Param("locationId") Long locationId,
                                 @Param("vaccineId") Long vaccineId);
}
