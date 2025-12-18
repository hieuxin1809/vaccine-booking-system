package com.hieu.Booking_System.model.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hieu.Booking_System.enums.AppointmentStatus;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppointmentUpdateRequest {
    Long user_id;
    Long location_id;
    List<Long> vaccine_ids;

    @JsonFormat(pattern = "HH:mm")
    LocalDate appointment_time;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate appointment_date;

    AppointmentStatus appointment_status = AppointmentStatus.PENDING;
    String note;
    BigDecimal total_price;
}
