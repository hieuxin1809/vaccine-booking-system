package com.hieu.Booking_System.entity;

import java.util.Date;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "inventory")
public class InventoryEntity extends BaseEntity {
    @Column(nullable = false)
    private int quantity;

    @Column(name = "expiry_date", nullable = false)
    private Date expiryDate;

    @Column(name = "min_stock_level")
    private int minStockLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private LocationEntity location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaccine_id")
    private VaccineEntity vaccine;
}
