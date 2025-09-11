package com.hieu.Booking_System.repository;

import com.hieu.Booking_System.entity.AppointmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity , Long> {
    @Query("select a from AppointmentEntity a where a.deletedAt is null ")
    List<AppointmentEntity> GetAllActiveAppointments();
    List<AppointmentEntity> findByUser_Id(Long id);
    List<AppointmentEntity> findByLocation_Id(Long id);
    List<AppointmentEntity> findByAppointmentDate(LocalDate localDate);
    boolean existsByLocation_IdAndAppointmentDateAndAppointmentTime(Long id, LocalDate localDate, LocalTime localTime);
}
