package com.hieu.Booking_System.model.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    @NotBlank(message = "NAME_REQUIRED")
    String name;

    @Size(min = 8, message = "PASSWORD_INVALID")
    String password;

    String address;

    List<String> roles;
}
