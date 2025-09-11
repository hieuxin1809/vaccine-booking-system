package com.hieu.Booking_System.model.request;


import jakarta.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocationCreateRequest {
    String name;
    String address;
    String phone;
}
