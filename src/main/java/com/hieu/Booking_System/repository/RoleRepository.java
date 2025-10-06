package com.hieu.Booking_System.repository;

import com.hieu.Booking_System.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, String> {
    @Query("select r from RoleEntity r where r.deletedAt is null ")
    List<RoleEntity> getAll();

    List<RoleEntity> findByName(String name);
}
