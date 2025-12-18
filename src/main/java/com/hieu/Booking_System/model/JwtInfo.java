package com.hieu.Booking_System.model;

import java.io.Serializable;
import java.util.Date;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtInfo implements Serializable {
    private String jwtId;
    private Date issueTime;
    private String email;
    private Date expiredTime;
}
