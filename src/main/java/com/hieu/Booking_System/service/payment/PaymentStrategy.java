package com.hieu.Booking_System.service.payment;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

public interface PaymentStrategy {
    /**
     * Tạo URL thanh toán
     */
    String createPaymentUrl(Long appointmentId, HttpServletRequest request) throws Exception;

    /**
     * Xử lý callback từ payment gateway
     */
    Map<String, String> handleCallback(Map<String, String> params);

    /**
     * Lấy tên payment gateway
     */
    String getGatewayName();
}
