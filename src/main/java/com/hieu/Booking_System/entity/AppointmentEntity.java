package com.hieu.Booking_System.entity;

import com.hieu.Booking_System.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "appointment")
@Data
public class AppointmentEntity extends BaseEntity{

    @Column(name = "appointment_date" , nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "appointment_time", nullable = false)
    private LocalTime appointmentTime;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus appointmentStatus;

    private String note;

    @Column(name = "total_price",nullable = false)
    private BigDecimal totalPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;

    @OneToMany( mappedBy = "appointment",fetch = FetchType.LAZY)
    private List<AppointmentVaccineEntity> appointmentVaccineEntities;

    @ManyToOne(fetch = FetchType.LAZY)
    private LocationEntity location;
}
