package com.hieu.Booking_System.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class VNPayConfig {

    @Value("${spring.vnpay.tmnCode}")
    private String vnpTmnCode;

    @Value("${spring.vnpay.hashSecret}")
    private String vnpHashSecret;

    @Value("${spring.vnpay.apiUrl}")
    private String vnpApiUrl;

    @Value("${spring.vnpay.returnUrl}")
    private String vnpReturnUrl;

    public static final String VERSION = "2.1.0";
    public static final String COMMAND = "pay";
    public static final String ORDER_TYPE = "other";
}