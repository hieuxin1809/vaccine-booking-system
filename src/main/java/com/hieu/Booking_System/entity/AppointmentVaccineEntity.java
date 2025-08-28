package com.hieu.Booking_System.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "appointment_vaccine")
public class AppointmentVaccineEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaccine_id")
    private VaccineEntity vaccine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private AppointmentEntity appointment;
}
