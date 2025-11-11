package com.hieu.Booking_System.model.response;

import com.hieu.Booking_System.enums.Role;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    Long id;
    String name;
    String email;
    String avatarUrl;
    String address;
    Set<RoleResponse> roles;
}
