package com.hieu.Booking_System.controller;

import com.hieu.Booking_System.entity.UserEntity;
import com.hieu.Booking_System.model.request.ChangePasswordRequest;
import com.hieu.Booking_System.model.request.UserCreateRequest;
import com.hieu.Booking_System.model.request.UserUpdateRequest;
import com.hieu.Booking_System.model.response.ApiResponse;
import com.hieu.Booking_System.model.response.UserResponse;
import com.hieu.Booking_System.service.CloudinaryService;
import com.hieu.Booking_System.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/user")
@Slf4j
public class UserController {
    UserService userService;
    CloudinaryService cloudinaryService;
    @PostMapping()
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreateRequest user) {
        return ApiResponse.<UserResponse>builder()
                .data(userService.createUser(user))
                .build();
    }
    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<List<UserResponse>> getAllUsers() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        log.info(authentication.getName());
        log.info(authentication.getPrincipal().toString());
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
    @PreAuthorize("hasRole('ADMIN')")
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
    @GetMapping("/myInfo")
    ApiResponse<UserResponse> GetMyInfo() {
        return ApiResponse.<UserResponse>builder()
                .data(userService.getMyInfo())
                .build();
    }
    @PutMapping("/{userId}/password")
    public ApiResponse<Void> changePassWord(
            @PathVariable Long userId,
            @RequestBody ChangePasswordRequest request
    ){
        userService.changePassword(userId, request);
        return ApiResponse.<Void>builder()
                .message("change password successfully")
                .build();
    }
    @PutMapping("/{userId}/avatar")
    public ApiResponse<UserResponse> updateAvatar(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file
    ) {
        return ApiResponse.<UserResponse>builder()
                .data(userService.uploadAvatar(userId, file))
                .build();
    }
}
