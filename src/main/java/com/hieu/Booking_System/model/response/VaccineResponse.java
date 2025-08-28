package com.hieu.Booking_System.model.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VaccineResponse {
    Long id;
    String name;
    BigDecimal price;
    int dosesRequired;
    String description;
}
