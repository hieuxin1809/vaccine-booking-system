package com.hieu.Booking_System.controller;

import com.hieu.Booking_System.model.request.LoginRequest;
import com.hieu.Booking_System.model.request.VerifyTokenRequest;
import com.hieu.Booking_System.model.request.LogoutRequest;
import com.hieu.Booking_System.model.request.RefreshRequest;
import com.hieu.Booking_System.model.response.ApiResponse;
import com.hieu.Booking_System.model.response.LoginResponse;
import com.hieu.Booking_System.model.response.RefreshTokenResponse;
import com.hieu.Booking_System.model.response.VerifyTokenResponse;
import com.hieu.Booking_System.service.AuthenticationService;
import com.hieu.Booking_System.service.JwtService;
import com.nimbusds.jose.JOSEException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;
    JwtService jwtService;
    @PostMapping("/log-in")
    ApiResponse<LoginResponse> authenticate(@RequestBody LoginRequest loginRequest) {
       return ApiResponse.<LoginResponse>builder()
               .data(authenticationService.authenticate(loginRequest))
               .build();
    }
    @PostMapping("/verify-token")
    ApiResponse<VerifyTokenResponse> verifyToken(@RequestBody VerifyTokenRequest request) throws ParseException, JOSEException {
        return ApiResponse.<VerifyTokenResponse>builder()
                .data(jwtService.verifyToken(request))
                .build();
    }
    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestHeader("Authorization") LogoutRequest logoutRequest) throws ParseException, JOSEException {
        authenticationService.logout(logoutRequest);
        return ApiResponse.<Void>builder()
                .build();
    }
    @PostMapping("/refresh")
    ApiResponse<RefreshTokenResponse> refreshToken(@RequestBody RefreshRequest request) throws ParseException, JOSEException {
        return ApiResponse.<RefreshTokenResponse>builder()
                .data(authenticationService.refresh(request))
                .build();
    }
}
