package com.hieu.Booking_System.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hieu.Booking_System.enums.AppointmentStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppointmentResponse {
    Long id;
    String userName;
    String userPhone;
    String locationName;
    String locationAddress;
    @JsonFormat(pattern = "HH:mm")
    LocalTime appointment_time;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate appointment_date;
    AppointmentStatus appointment_status;
    String note;
    BigDecimal totalPrice;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createAt;
    List<VaccineResponse> vaccines;
}
