package com.hieu.Booking_System.repository;

import com.hieu.Booking_System.entity.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<LocationEntity, Long> {
    @Query("select l from LocationEntity l where l.deletedAt is null ")
    List<LocationEntity> getAllLocationActive();
}
