package com.hieu.Booking_System.controller;

import com.hieu.Booking_System.enums.AppointmentStatus;
import com.hieu.Booking_System.model.request.AppointmentCreateRequest;
import com.hieu.Booking_System.model.request.AppointmentUpdateRequest;
import com.hieu.Booking_System.model.response.ApiResponse;
import com.hieu.Booking_System.model.response.AppointmentResponse;
import com.hieu.Booking_System.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/appointment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppointmentController {
    AppointmentService appointmentService;
    @PostMapping
    ApiResponse<AppointmentResponse> createAppointment(@RequestBody @Valid AppointmentCreateRequest appointmentCreateRequest){
        return ApiResponse.<AppointmentResponse>builder()
                .data(appointmentService.createAppointment(appointmentCreateRequest))
                .build();
    }
    @GetMapping("/{appointmentId}")
    ApiResponse<AppointmentResponse> getAppointmentById(@PathVariable Long appointmentId){
        return ApiResponse.<AppointmentResponse>builder()
                .data(appointmentService.getAppointmentById(appointmentId))
                .build();
    }
    @GetMapping()
    ApiResponse<List<AppointmentResponse>> getAllAppointment(){
        return ApiResponse.<List<AppointmentResponse>>builder()
                .data(appointmentService.getAllAppointments())
                .build();
    }
    @DeleteMapping("/{appointmentId}")
    ApiResponse<AppointmentResponse> deleteAppointment(@PathVariable Long appointmentId){
        appointmentService.deleteAppointment(appointmentId);
        return ApiResponse.<AppointmentResponse>builder()
                .build();
    }
    @GetMapping("/user/{userId}")
    ApiResponse<List<AppointmentResponse>> getAllAppointmentByUserId(@PathVariable Long userId){
        return ApiResponse.<List<AppointmentResponse>>builder()
                .data(appointmentService.getAllAppointmentsByUserId(userId))
                .build();
    }
    @GetMapping("/location/{locationId}")
    ApiResponse<List<AppointmentResponse>> getAllAppointmentByLocation(@PathVariable Long locationId){
        return ApiResponse.<List<AppointmentResponse>>builder()
                .data(appointmentService.getAllAppointmentsByLocationId(locationId))
                .build();
    }
    @PatchMapping("/{id}/status")
    ApiResponse<AppointmentResponse> updateAppointmentStatus(@PathVariable Long id, @RequestParam AppointmentStatus status){
        return ApiResponse.<AppointmentResponse>builder()
                .data(appointmentService.updateAppointmentStatus(id, status))
                .build();
    }
    @GetMapping("/date/{date}")
    ApiResponse<List<AppointmentResponse>> getAllAppointmentByDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date){
        return ApiResponse.<List<AppointmentResponse>>builder()
                .data(appointmentService.getAllAppointmentByDate(date))
                .build();
    }
}
