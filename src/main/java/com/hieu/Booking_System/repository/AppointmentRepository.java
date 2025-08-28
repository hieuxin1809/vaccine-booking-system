package com.hieu.Booking_System.repository;

import com.hieu.Booking_System.entity.AppointmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity , Long> {
}
