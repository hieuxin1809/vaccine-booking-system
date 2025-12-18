package com.hieu.Booking_System.validation.annotation;

import java.lang.annotation.*;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import com.hieu.Booking_System.validation.validator.AppointmentDatetimeValidator;

@Documented
@Constraint(validatedBy = {AppointmentDatetimeValidator.class})
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAppointmentDatetime {
    String message() default "INVALID_DATETIME";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
