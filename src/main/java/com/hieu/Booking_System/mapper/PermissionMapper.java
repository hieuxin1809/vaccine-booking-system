package com.hieu.Booking_System.mapper;

import org.mapstruct.Mapper;

import com.hieu.Booking_System.entity.PermissionEntity;
import com.hieu.Booking_System.model.request.PermissionRequest;
import com.hieu.Booking_System.model.response.PermissionResponse;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    PermissionEntity toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(PermissionEntity permission);
}
