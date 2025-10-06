package com.hieu.Booking_System.mapper;

import com.hieu.Booking_System.entity.PermissionEntity;
import com.hieu.Booking_System.model.request.PermissionRequest;
import com.hieu.Booking_System.model.response.PermissionResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    PermissionEntity toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(PermissionEntity permission);
}
