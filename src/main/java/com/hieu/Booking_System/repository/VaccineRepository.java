package com.hieu.Booking_System.repository;

import com.hieu.Booking_System.entity.VaccineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VaccineRepository extends JpaRepository<VaccineEntity,Long> {
    @Query("select v from VaccineEntity v where v.deletedAt is null")
    List<VaccineEntity> GetAllActiveVaccine();
}
