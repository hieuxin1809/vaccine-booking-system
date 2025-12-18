package com.hieu.Booking_System.model.response;

import java.io.Serializable;
import java.util.Set;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse implements Serializable {
    // Thêm dòng này nếu class có con
    private static final long serialVersionUID = 1L;

    Long id;
    String name;
    String email;
    String avatarUrl;
    String address;
    Set<RoleResponse> roles;
}
