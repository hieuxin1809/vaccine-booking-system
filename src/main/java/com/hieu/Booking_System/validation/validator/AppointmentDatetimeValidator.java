package com.hieu.Booking_System.validation.validator;

import com.hieu.Booking_System.model.request.AppointmentCreateRequest;
import com.hieu.Booking_System.validation.annotation.ValidAppointmentDatetime;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

public class AppointmentDatetimeValidator implements ConstraintValidator<ValidAppointmentDatetime, AppointmentCreateRequest> {

    @Override
    public boolean isValid(AppointmentCreateRequest appointmentCreateRequest, ConstraintValidatorContext constraintValidatorContext) {
        if(appointmentCreateRequest.getAppointment_date() == null || appointmentCreateRequest.getAppointment_time() == null){
            return true;
        }
        LocalDateTime appointmentDatetime = LocalDateTime.
                of(appointmentCreateRequest.getAppointment_date(), appointmentCreateRequest.getAppointment_time());
        return !appointmentDatetime.isBefore(LocalDateTime.now());
    }

}
