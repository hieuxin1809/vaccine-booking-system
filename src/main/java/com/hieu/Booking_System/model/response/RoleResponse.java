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
public class RoleResponse implements Serializable {
    // Thêm dòng này nếu class có con
    private static final long serialVersionUID = 1L;
    String name;
    String description;
    Set<PermissionResponse> permissions;
}
