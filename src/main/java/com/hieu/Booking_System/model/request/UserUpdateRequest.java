package com.hieu.Booking_System.model.request;

import com.hieu.Booking_System.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    @NotBlank(message = "NAME_REQUIRED")
    String name;

    @Size(min = 8,message = "PASSWORD_INVALID")
    String password;

    String address;

    List<String> roles;
}
