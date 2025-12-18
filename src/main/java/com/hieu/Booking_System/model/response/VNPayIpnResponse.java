package com.hieu.Booking_System.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VNPayIpnResponse {
    private String RspCode;
    private String Message;
}
