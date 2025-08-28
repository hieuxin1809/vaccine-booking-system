package com.hieu.Booking_System.entity;

import com.hieu.Booking_System.enums.AppointmentType;
import jakarta.persistence.*;

@Entity
@Table(name = "notification")
public class NotificationEntity extends BaseEntity{
    private String title;
    private String content;

    @Enumerated(EnumType.STRING)
    private AppointmentType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
}
