package com.hieu.Booking_System.controller;

import com.hieu.Booking_System.model.request.LocationCreateRequest;
import com.hieu.Booking_System.model.request.LocationUpdateRequest;
import com.hieu.Booking_System.model.request.UserCreateRequest;
import com.hieu.Booking_System.model.request.UserUpdateRequest;
import com.hieu.Booking_System.model.response.ApiResponse;
import com.hieu.Booking_System.model.response.LocationResponse;
import com.hieu.Booking_System.model.response.UserResponse;
import com.hieu.Booking_System.service.LocationService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/location")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocationController {
    LocationService locationService;
    @PostMapping()
    ApiResponse<LocationResponse> createLocation(@RequestBody @Valid LocationCreateRequest locationCreateRequest) {
        return ApiResponse.<LocationResponse>builder()
                .data(locationService.createLocation(locationCreateRequest))
                .build();
    }
    @GetMapping()
    ApiResponse<List<LocationResponse>> getAllLocations() {
        return ApiResponse.<List<LocationResponse>>builder()
                .data(locationService.getAllLocations())
                .build();
    }
    @GetMapping("/{id}")
    ApiResponse<LocationResponse> getLocation(@PathVariable Long id) {
        return ApiResponse.<LocationResponse>builder()
                .data(locationService.getLocationById(id))
                .build();
    }
    @DeleteMapping("/{id}")
    ApiResponse<Void> deleteLocation(@PathVariable Long id){
        locationService.deleteLocation(id);
        return ApiResponse.<Void>builder()
                .build();
    }
    @PutMapping("/{userId}")
    ApiResponse<LocationResponse> updateLocation(@PathVariable("userId") Long locationId, @RequestBody LocationUpdateRequest locationUpdateRequest) {
        return ApiResponse.<LocationResponse>builder()
                .data(locationService.updateLocation(locationId,locationUpdateRequest))
                .build();
    }
}
