package com.hieu.Booking_System.service;
import com.hieu.Booking_System.configuration.VNPayConfig;
import com.hieu.Booking_System.entity.AppointmentEntity;
import com.hieu.Booking_System.entity.PaymentEntity;
import com.hieu.Booking_System.enums.PaymentGateway;
import com.hieu.Booking_System.enums.PaymentStatus;
import com.hieu.Booking_System.repository.AppointmentRepository;
import com.hieu.Booking_System.repository.PaymentRepository;
import com.hieu.Booking_System.service.payment.PayPalStrategy;
import com.hieu.Booking_System.service.payment.PaymentStrategy;
import com.hieu.Booking_System.service.payment.VNPayStrategy;
import com.hieu.Booking_System.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final VNPayConfig vnPayConfig;
    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final VNPayStrategy vnPayStrategy;
    private final PayPalStrategy payPalStrategy;

    /**
     * Tạo payment URL
     */
    public String createPaymentUrl(Long appointmentId,
                                   PaymentGateway gateway,
                                   HttpServletRequest request) throws Exception {
        log.info("Creating payment URL - Appointment: {}, Gateway: {}", appointmentId, gateway);
        PaymentStrategy strategy = getStrategy(gateway);
        return strategy.createPaymentUrl(appointmentId, request);
    }

    /**
     * Xử lý callback
     */
    public Map<String, String> handleCallback(PaymentGateway gateway,
                                              Map<String, String> params) {
        log.info("Handling callback - Gateway: {}", gateway);
        PaymentStrategy strategy = getStrategy(gateway);
        return strategy.handleCallback(params);
    }

    /**
     * Get strategy theo gateway
     */
    private PaymentStrategy getStrategy(PaymentGateway gateway) {
        return switch (gateway) {
            case VNPAY -> vnPayStrategy;
            case PAYPAL -> payPalStrategy;
            default -> throw new IllegalArgumentException("Unsupported gateway: " + gateway);
        };
    }
}
