package com.hieu.Booking_System.model.request;

import java.util.Date;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryRequest {
    private Long locationId;

    private Long vaccineId;

    private int quantity;

    private Date expiryDate;

    private int minStockLevel = 10;
}
