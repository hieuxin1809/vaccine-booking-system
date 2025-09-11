package com.hieu.Booking_System.model.request;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.hieu.Booking_System.enums.AppointmentStatus;
import com.hieu.Booking_System.validation.annotation.ValidAppointmentDatetime;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@ValidAppointmentDatetime(message = "INVALID_DATETIME")
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppointmentCreateRequest {
    Long user_id;
    Long location_id;
    List<Long> vaccine_ids;
    @JsonFormat(pattern = "HH:mm")
    LocalTime appointment_time;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate appointment_date;
    @Builder.Default
    AppointmentStatus appointment_status = AppointmentStatus.PENDING;
    String note;
}
