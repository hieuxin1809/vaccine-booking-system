package com.hieu.Booking_System.controller;

import com.hieu.Booking_System.model.request.PermissionRequest;
import com.hieu.Booking_System.model.request.RoleRequest;
import com.hieu.Booking_System.model.response.ApiResponse;
import com.hieu.Booking_System.model.response.PermissionResponse;
import com.hieu.Booking_System.model.response.RoleResponse;
import com.hieu.Booking_System.service.PermissionService;
import com.hieu.Booking_System.service.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/permission")
public class PermissionController {
    PermissionService permissionService;

    @PostMapping
    ApiResponse<PermissionResponse> create(@RequestBody PermissionRequest permissionRequest) {
        return ApiResponse.<PermissionResponse>builder()
                .data(permissionService.createPermission(permissionRequest))
                .build();
    }

    @GetMapping
    ApiResponse<List<PermissionResponse>> getAll() {
        return ApiResponse.<List<PermissionResponse>>builder()
                .data(permissionService.getAllPermissions())
                .build();
    }

    @DeleteMapping("/{permission}")
    ApiResponse<Void> delete(@PathVariable("permission") String role) {
        permissionService.deletePermission(role);
        return ApiResponse.<Void>builder().build();
    }
}
