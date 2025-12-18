package com.hieu.Booking_System.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hieu.Booking_System.entity.VaccineEntity;

@Repository
public interface VaccineRepository extends JpaRepository<VaccineEntity, Long> {
    @Query("select v from VaccineEntity v where v.deletedAt is null")
    List<VaccineEntity> GetAllActiveVaccine();
}
