package com.hieu.Booking_System.model.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PermissionResponse implements Serializable {
    // Thêm dòng này nếu class có con
    private static final long serialVersionUID = 1L;
    String name;
    String description;
}
