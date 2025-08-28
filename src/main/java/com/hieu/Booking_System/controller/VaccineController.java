package com.hieu.Booking_System.controller;

import com.hieu.Booking_System.model.request.UserUpdateRequest;
import com.hieu.Booking_System.model.request.VaccineCreateRequest;
import com.hieu.Booking_System.model.request.VaccineUpdateRequest;
import com.hieu.Booking_System.model.response.ApiResponse;
import com.hieu.Booking_System.model.response.UserResponse;
import com.hieu.Booking_System.model.response.VaccineResponse;
import com.hieu.Booking_System.service.VaccineService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/vaccine")
public class VaccineController {
    VaccineService vaccineService;
    @PostMapping()
    ApiResponse<VaccineResponse> createVaccine(@RequestBody @Valid VaccineCreateRequest vaccineCreateRequest) {
        return ApiResponse.<VaccineResponse>builder()
                .data(vaccineService.createVaccine(vaccineCreateRequest))
                .build();
    }
    @GetMapping()
    ApiResponse<List<VaccineResponse>> getAllVaccine() {
        return ApiResponse.<List<VaccineResponse>>builder()
                .data(vaccineService.getAllVaccine())
                .build();
    }
    @GetMapping("/{vaccineId}")
    ApiResponse<VaccineResponse> getVaccineById(@PathVariable Long vaccineId) {
        return ApiResponse.<VaccineResponse>builder()
                .data(vaccineService.getVaccineById(vaccineId))
                .build();
    }
    @DeleteMapping("/{vaccineId}")
    ApiResponse<Void> deleteVaccineById(@PathVariable Long vaccineId) {
        vaccineService.deleteVaccineById(vaccineId);
        return ApiResponse.<Void>builder()
                .build();
    }
    @PutMapping("/{vaccineId}")
    ApiResponse<VaccineResponse> updateVaccine(@PathVariable("vaccineId") Long vaccineId, @RequestBody VaccineUpdateRequest vaccineUpdateRequest) {
        return ApiResponse.<VaccineResponse>builder()
                .data(vaccineService.updateVaccine(vaccineId, vaccineUpdateRequest))
                .build();
    }
}
