package com.hieu.Booking_System.exception;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999,"Uncategorized exception" , HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1000,"Invalid Key" , HttpStatus.NOT_FOUND),
    USER_NOT_FOUND(1001,"User not found" , HttpStatus.NOT_FOUND),
    EMAIL_EXIST(1002,"Email Exist" , HttpStatus.BAD_REQUEST),
    EMAIL_INVALID(1003,"Email Invalid" , HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1004,"Password Invalid" , HttpStatus.BAD_REQUEST),
    VACCINE_NOT_FOUND(1005,"Vaccine not found" , HttpStatus.NOT_FOUND),
    LOCATION_NOT_FOUND(1006,"Location not found" , HttpStatus.NOT_FOUND),
    ;
    private int code;
    private String message;
    private HttpStatusCode httpStatusCode;

    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}
