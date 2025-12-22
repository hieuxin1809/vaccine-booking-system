package com.hieu.Booking_System.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hieu.Booking_System.entity.AppointmentEntity;
import com.hieu.Booking_System.enums.AppointmentStatus;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Long> {
    @Query(
            """
		SELECT a
		FROM AppointmentEntity a
		JOIN FETCH a.user
		JOIN FETCH a.location
		LEFT JOIN FETCH a.appointmentVaccineEntities av
		LEFT JOIN FETCH av.vaccine
		WHERE a.deletedAt IS NULL
	""")
    List<AppointmentEntity> GetAllActiveAppointments();
	@Query(
			"""
        SELECT a
        FROM AppointmentEntity a
        JOIN FETCH a.user
        JOIN FETCH a.location
        LEFT JOIN FETCH a.appointmentVaccineEntities av
        LEFT JOIN FETCH av.vaccine
        WHERE a.deletedAt IS NULL
    """)
	Page<AppointmentEntity> getAllActiveAppointments(Pageable pageable);

    List<AppointmentEntity> findByUser_Id(Long id);

    List<AppointmentEntity> findByLocation_Id(Long id);

    List<AppointmentEntity> findByAppointmentDate(LocalDate localDate);

    boolean existsByLocation_IdAndAppointmentDateAndAppointmentTime(Long id, LocalDate localDate, LocalTime localTime);

    List<AppointmentEntity> findAllByAppointmentStatusAndCreatedAtBefore(AppointmentStatus status, LocalDateTime time);
}
