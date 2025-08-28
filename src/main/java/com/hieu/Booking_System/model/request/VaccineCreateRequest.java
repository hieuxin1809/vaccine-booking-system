package com.hieu.Booking_System.model.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VaccineCreateRequest {
    String name;
    BigDecimal price;
    int dosesRequired;
    String description;
}
