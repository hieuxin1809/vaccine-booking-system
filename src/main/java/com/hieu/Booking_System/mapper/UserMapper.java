package com.hieu.Booking_System.mapper;

import org.mapstruct.Mapper;

import com.hieu.Booking_System.entity.UserEntity;
import com.hieu.Booking_System.model.request.UserCreateRequest;
import com.hieu.Booking_System.model.response.UserResponse;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserEntity toUserEntity(UserCreateRequest userCreateRequest);

    UserResponse toUserResponse(UserEntity userEntity);
    //    @Mapping(target = "roles", ignore = true)
    //    void updateUserEntity(@MappingTarget UserEntity userEntity, UserUpdateRequest userUpdateRequest);
}
