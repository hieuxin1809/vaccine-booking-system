package com.hieu.Booking_System.entity;

import java.time.LocalTime;

import jakarta.persistence.*;

import com.hieu.Booking_System.enums.DayOfWeek;

@Entity
@Table(name = "working_hour")
public class WorkingHourEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private LocationEntity location;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    @Column(name = "is_closed")
    private boolean isClosed;
}
