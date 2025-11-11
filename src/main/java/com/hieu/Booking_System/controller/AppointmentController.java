package com.hieu.Booking_System.controller;

import com.hieu.Booking_System.enums.AppointmentStatus;
import com.hieu.Booking_System.enums.PaymentGateway;
import com.hieu.Booking_System.model.request.AppointmentCreateRequest;
import com.hieu.Booking_System.model.request.AppointmentUpdateRequest;
import com.hieu.Booking_System.model.response.ApiResponse;
import com.hieu.Booking_System.model.response.AppointmentResponse;
import com.hieu.Booking_System.service.AppointmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/appointment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppointmentController {
    AppointmentService appointmentService;
//    @PostMapping
//    ApiResponse<AppointmentResponse> createAppointment(@RequestBody @Valid AppointmentCreateRequest appointmentCreateRequest){
//        return ApiResponse.<AppointmentResponse>builder()
//                .data(appointmentService.createAppointment(appointmentCreateRequest))
//                .build();
//    }
    @PostMapping
    public ApiResponse<Map<String, Object>> createAppointment(
            @RequestBody @Valid AppointmentCreateRequest request,
            @RequestParam(defaultValue = "VNPAY") PaymentGateway gateway,
            HttpServletRequest httpRequest) {

        Map<String, Object> result = appointmentService.createAppointmentWithPayment(
                request,
                gateway,
                httpRequest);

        return ApiResponse.<Map<String, Object>>builder()
                .code(200)
                .message("Tạo lịch hẹn và URL thanh toán thành công")
                .data(result)
                .build();
    }
    @GetMapping("/{appointmentId}")
    ApiResponse<AppointmentResponse> getAppointmentById(@PathVariable Long appointmentId){
        return ApiResponse.<AppointmentResponse>builder()
                .data(appointmentService.getAppointmentById(appointmentId))
                .build();
    }
    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
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
    // sửa đổi trạng thái
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
