package com.hieu.Booking_System.model.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryResponse {
    private Long id;
    private int quantity;
    private Date expiryDate;
    private int minStockLevel;

    private LocationResponse location;
    private VaccineResponse vaccine;
    
}
