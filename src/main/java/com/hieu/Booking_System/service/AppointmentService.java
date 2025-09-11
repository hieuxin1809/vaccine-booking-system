package com.hieu.Booking_System.service;

import com.hieu.Booking_System.enums.AppointmentStatus;
import com.hieu.Booking_System.model.request.AppointmentCreateRequest;
import com.hieu.Booking_System.model.response.AppointmentResponse;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {
    AppointmentResponse createAppointment(AppointmentCreateRequest request);
    void deleteAppointment(Long id);
    List<AppointmentResponse> getAllAppointments();
    AppointmentResponse getAppointmentById(Long id);
    List<AppointmentResponse> getAllAppointmentsByUserId(Long id);
    List<AppointmentResponse> getAllAppointmentsByLocationId(Long id);
    AppointmentResponse updateAppointmentStatus(Long id, AppointmentStatus status);
    List<AppointmentResponse> getAllAppointmentByDate(LocalDate date);
}
