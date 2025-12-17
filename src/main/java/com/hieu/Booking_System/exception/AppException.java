package com.hieu.Booking_System.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AppException extends RuntimeException{
    private ErrorCode errorCode;
    public AppException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    public AppException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause); // Truyền 'cause' cho lớp cha RuntimeException quản lý
        this.errorCode = errorCode;
    }
}
