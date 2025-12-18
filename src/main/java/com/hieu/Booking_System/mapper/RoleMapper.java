package com.hieu.Booking_System.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hieu.Booking_System.entity.RoleEntity;
import com.hieu.Booking_System.model.request.RoleRequest;
import com.hieu.Booking_System.model.response.RoleResponse;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    RoleEntity toRole(RoleRequest request);

    RoleResponse toRoleResponse(RoleEntity role);
}
