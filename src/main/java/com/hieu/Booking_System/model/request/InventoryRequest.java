package com.hieu.Booking_System.model.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;

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
