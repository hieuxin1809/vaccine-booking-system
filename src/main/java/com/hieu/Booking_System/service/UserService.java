package com.hieu.Booking_System.service;

import com.hieu.Booking_System.entity.UserEntity;
import com.hieu.Booking_System.model.request.UserCreateRequest;
import com.hieu.Booking_System.model.request.UserUpdateRequest;
import com.hieu.Booking_System.model.response.UserResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserCreateRequest user);
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    void deleteUserById(Long id);
    UserResponse updateUserById(Long id, UserUpdateRequest userUpdateRequest);
}
