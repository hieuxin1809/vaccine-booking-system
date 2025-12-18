package com.hieu.Booking_System.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.hieu.Booking_System.model.request.VaccineCreateRequest;
import com.hieu.Booking_System.model.request.VaccineUpdateRequest;
import com.hieu.Booking_System.model.response.ApiResponse;
import com.hieu.Booking_System.model.response.VaccineResponse;
import com.hieu.Booking_System.service.VaccineService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/vaccine")
public class VaccineController {
    VaccineService vaccineService;

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<VaccineResponse> createVaccine(@RequestBody @Valid VaccineCreateRequest vaccineCreateRequest) {
        return ApiResponse.<VaccineResponse>builder()
                .data(vaccineService.createVaccine(vaccineCreateRequest))
                .build();
    }

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<List<VaccineResponse>> getAllVaccine() {
        return ApiResponse.<List<VaccineResponse>>builder()
                .data(vaccineService.getAllVaccine())
                .build();
    }

    @GetMapping("/{vaccineId}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<VaccineResponse> getVaccineById(@PathVariable Long vaccineId) {
        return ApiResponse.<VaccineResponse>builder()
                .data(vaccineService.getVaccineById(vaccineId))
                .build();
    }

    @DeleteMapping("/{vaccineId}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<Void> deleteVaccineById(@PathVariable Long vaccineId) {
        vaccineService.deleteVaccineById(vaccineId);
        return ApiResponse.<Void>builder().build();
    }

    @PutMapping("/{vaccineId}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<VaccineResponse> updateVaccine(
            @PathVariable("vaccineId") Long vaccineId, @Valid @RequestBody VaccineUpdateRequest vaccineUpdateRequest) {
        return ApiResponse.<VaccineResponse>builder()
                .data(vaccineService.updateVaccine(vaccineId, vaccineUpdateRequest))
                .build();
    }
}
