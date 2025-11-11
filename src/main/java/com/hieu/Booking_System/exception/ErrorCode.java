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
    APPOINTMENT_NOT_FOUND(1007,"Appointment not found" , HttpStatus.NOT_FOUND),
    APPOINTMENT_DUPLICATED(1008,"Appointment already ordered" , HttpStatus.CONFLICT),
    APPOINTMENT_CONFLICT(1009,"Appointment Conflict" , HttpStatus.CONFLICT),
    INTERRUPTED_LOCK(1010,"Interrupted while waiting for lock" , HttpStatus.BAD_REQUEST),
    INVALID_DATETIME(1011,"DateTime invalid" , HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(1012,"Role not found" , HttpStatus.NOT_FOUND),
    PERMISSION_NOT_FOUND(1013,"Permission not found" , HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1014,"Unauthenticated" , HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1015,"UNAUTHORIZED" , HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(1016,"Token expired" , HttpStatus.UNAUTHORIZED),
    INVENTORY_EXIST(1017,"Inventory already exists for this location and vaccine" , HttpStatus.BAD_REQUEST),
    INVENTORY_NOT_FOUND(1018,"Inventory not found" , HttpStatus.NOT_FOUND),
    OUT_OF_STOCK(1019,"Out of stock" , HttpStatus.BAD_REQUEST),
    INVALID_TOKEN_REGISTER(1020,"Invalid token register" , HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_VERIFIED(1021,"Email already verified" , HttpStatus.BAD_REQUEST),
    EMAIL_NOT_VERIFIED(1022,"Email not verified" , HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR(1023,"Internal server error" , HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_OLD_PASSWORD(1024, "invalid old password", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS(1025 , "email or password is incorrect", HttpStatus.UNAUTHORIZED)

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
