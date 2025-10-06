package com.hieu.Booking_System.repository;

import com.hieu.Booking_System.entity.PermissionEntity;
import com.hieu.Booking_System.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<PermissionEntity , String> {
    @Query("select p from PermissionEntity p where p.deletedAt is null ")
    List<PermissionEntity> getAll();
}
