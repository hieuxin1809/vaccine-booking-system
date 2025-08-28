package com.hieu.Booking_System.model.request;

import com.hieu.Booking_System.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreateRequest {
    String name;
    @Email(message = "EMAIL_INVALID")
    String email;
    @Size(min = 8,message = "PASSWORD_INVALID")
    String password;
    String address;
    Role role;
}
