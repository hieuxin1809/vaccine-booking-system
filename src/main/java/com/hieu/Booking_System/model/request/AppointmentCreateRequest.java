package com.hieu.Booking_System.model.request;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.hieu.Booking_System.enums.AppointmentStatus;
import com.hieu.Booking_System.validation.annotation.ValidAppointmentDatetime;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
    @NotNull(message = "USER_ID_REQUIRED")
    Long user_id;

    @NotNull(message = "LOCATION_ID_REQUIRED")
    Long location_id;
    @NotEmpty(message = "VACCINE_IDS_REQUIRED")
    List<Long> vaccine_ids;

    @NotNull(message = "APPOINTMENT_TIME_REQUIRED")
    @JsonFormat(pattern = "HH:mm")
    LocalTime appointment_time;

    @NotNull(message = "APPOINTMENT_DATE_REQUIRED")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @FutureOrPresent(message = "APPOINTMENT_DATE_MUST_BE_IN_FUTURE")
    LocalDate appointment_date;

    @Builder.Default
    AppointmentStatus appointment_status = AppointmentStatus.PENDING;
    String note;
}
