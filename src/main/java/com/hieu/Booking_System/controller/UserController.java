package com.hieu.Booking_System.controller;

import com.hieu.Booking_System.entity.UserEntity;
import com.hieu.Booking_System.model.request.UserCreateRequest;
import com.hieu.Booking_System.model.request.UserUpdateRequest;
import com.hieu.Booking_System.model.response.ApiResponse;
import com.hieu.Booking_System.model.response.UserResponse;
import com.hieu.Booking_System.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/user")
public class UserController {
    UserService userService;
    @PostMapping()
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreateRequest user) {
        return ApiResponse.<UserResponse>builder()
                .data(userService.createUser(user))
                .build();
    }
    @GetMapping()
    ApiResponse<List<UserResponse>> getAllUsers() {
        return ApiResponse.<List<UserResponse>>builder()
                .data(userService.getAllUsers())
                .build();
    }
    @GetMapping("/{id}")
    ApiResponse<UserResponse> getUser(@PathVariable Long id) {
        return ApiResponse.<UserResponse>builder()
                .data(userService.getUserById(id))
                .build();
    }
    @DeleteMapping("/{id}")
    ApiResponse<Void> deleteUser(@PathVariable Long id){
        userService.deleteUserById(id);
        return ApiResponse.<Void>builder()
                .build();
    }
    @PutMapping("/{userId}")
    ApiResponse<UserResponse> updateUser(@PathVariable("userId") Long userId, @RequestBody UserUpdateRequest userUpdateRequest) {
        return ApiResponse.<UserResponse>builder()
                .data(userService.updateUserById(userId,userUpdateRequest))
                .build();
    }
}
