package com.hieu.Booking_System.entity;

import com.hieu.Booking_System.enums.DayOfWeek;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "location")
@Data
public class LocationEntity extends BaseEntity{
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String phone;

    @OneToMany(mappedBy = "location" , fetch = FetchType.LAZY)
    private List<AppointmentEntity> appointments;

    @OneToMany(mappedBy = "location" , fetch = FetchType.LAZY)
    private List<WorkingHourEntity> workingHours;

    @OneToMany(mappedBy = "location" , fetch = FetchType.LAZY)
    private List<InventoryEntity> inventory;
}
