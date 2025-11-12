package com.hieu.Booking_System.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleRequest {
    @NotBlank(message = "NAME_REQUIRED")
    String name;
    String description;
    @NotNull(message = "PERMISSION_REQUIRED")
    Set<String> permissions;
}
