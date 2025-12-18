package com.hieu.Booking_System.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hieu.Booking_System.entity.RoleEntity;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, String> {
    @Query("select r from RoleEntity r where r.deletedAt is null ")
    List<RoleEntity> getAll();

    @Query("SELECT r FROM RoleEntity r LEFT JOIN FETCH r.permissions WHERE r.name = :name")
    Optional<RoleEntity> findByName(String name);
}
