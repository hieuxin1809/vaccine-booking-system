package com.hieu.Booking_System.entity;

import com.hieu.Booking_System.enums.Role;
import jakarta.persistence.*;
import jdk.jfr.DataAmount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity extends BaseEntity {
    private String name;

    private String password;
    @Column(unique = true, nullable = false)
    private String email;

    private String address;
    private String phone;
    private LocalDate dob;
    private Character gender;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user" , fetch = FetchType.LAZY)
    private List<AppointmentEntity> patientAppointments;

    @OneToMany(mappedBy = "user" , fetch = FetchType.LAZY)
    private List<NotificationEntity> notifications;
}
