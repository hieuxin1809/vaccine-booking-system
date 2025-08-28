package com.hieu.Booking_System.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "vaccine")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaccineEntity extends BaseEntity{
    private String name;
    private String description;
    private String manufacturer;
    private BigDecimal price;

    @Column(name = "doses_required")
    private int dosesRequired;
    @Column(name = "interval_day")
    private int intervalDay;

    @OneToMany(mappedBy = "vaccine" , fetch = FetchType.LAZY)
    private List<AppointmentVaccineEntity> appointmentVaccine;
}
