package com.hieu.Booking_System.model.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleResponse {
    String name;
    String description;
    Set<PermissionResponse> permissions;
}
