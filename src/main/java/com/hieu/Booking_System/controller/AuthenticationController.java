package com.hieu.Booking_System.controller;

import java.text.ParseException;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.hieu.Booking_System.model.request.*;
import com.hieu.Booking_System.model.response.*;
import com.hieu.Booking_System.service.AuthenticationService;
import com.hieu.Booking_System.service.JwtService;
import com.nimbusds.jose.JOSEException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;
    JwtService jwtService;

    @PostMapping("/log-in")
    ApiResponse<LoginResponse> authenticate(@Valid @RequestBody LoginRequest loginRequest) {
        return ApiResponse.<LoginResponse>builder()
                .data(authenticationService.login(loginRequest))
                .build();
    }

    @PostMapping("/register")
    ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.<RegisterResponse>builder()
                .data(authenticationService.register(request))
                .build();
    }

    @PostMapping("/verify-token")
    ApiResponse<VerifyTokenResponse> verifyToken(@RequestBody VerifyTokenRequest request)
            throws ParseException, JOSEException {
        return ApiResponse.<VerifyTokenResponse>builder()
                .data(jwtService.verifyToken(request))
                .build();
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    ApiResponse<Void> logout(@RequestHeader("Authorization") LogoutRequest logoutRequest)
            throws ParseException, JOSEException {
        authenticationService.logout(logoutRequest);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/refresh")
    ApiResponse<RefreshTokenResponse> refreshToken(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {
        return ApiResponse.<RefreshTokenResponse>builder()
                .data(authenticationService.refresh(request))
                .build();
    }

    @GetMapping("/verify")
    ApiResponse<Void> verifyEmail(@RequestParam("token") String token) {
        authenticationService.verifyEmail(token);
        // Tùy chọn: Chuyển hướng người dùng đến trang thành công
        return ApiResponse.<Void>builder()
                .message("Xác nhận email thành công. Bạn đã có thể đăng nhập.")
                .build();
    }
}
