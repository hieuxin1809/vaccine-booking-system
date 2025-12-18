package com.hieu.Booking_System.model.request;

import jakarta.validation.constraints.NotBlank;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocationUpdateRequest {
    @NotBlank(message = "NAME_REQUIRED")
    String name;

    @NotBlank(message = "ADDRESS_REQUIRED")
    String address;

    String phone;
}
