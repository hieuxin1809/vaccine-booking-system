package com.hieu.Booking_System.model.response;

import java.util.Date;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VerifyTokenResponse {
    private boolean isValid;
    private String email;
    private Date expiredTime;
    private List<String> scopes;
}
