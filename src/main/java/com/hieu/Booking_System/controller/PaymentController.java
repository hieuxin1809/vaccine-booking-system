package com.hieu.Booking_System.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import com.hieu.Booking_System.aspect.LogTime;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import com.hieu.Booking_System.enums.PaymentGateway;
import com.hieu.Booking_System.model.response.ApiResponse;
import com.hieu.Booking_System.model.response.VNPayIpnResponse;
import com.hieu.Booking_System.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
public class    PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<String> createPayment(
            @RequestParam Long appointmentId,
            @RequestParam(defaultValue = "VNPAY") PaymentGateway gateway,
            HttpServletRequest request) {
        try {
            String paymentUrl = paymentService.createPaymentUrl(appointmentId, gateway, request);
            return ApiResponse.<String>builder()
                    .code(200)
                    .message("Tạo URL thanh toán " + gateway + " thành công")
                    .data(paymentUrl)
                    .build();
        } catch (Exception e) {
            log.error("Error creating payment", e);
            return ApiResponse.<String>builder()
                    .code(500)
                    .message("Lỗi: " + e.getMessage())
                    .build();
        }
    }

    /**
     * VNPay callback
     */
    @GetMapping("/vnpay/callback")
    public RedirectView vnpayCallback(HttpServletRequest request) {
        log.info("========== VNPAY CALLBACK ==========");
        Map<String, String> params = extractParams(request);
        Map<String, String> result = paymentService.handleCallback(PaymentGateway.VNPAY, params);
        return redirectToFrontend(result);
    }

    /**
     * PayPal success callback
     */
    @GetMapping("/paypal/success")
    public RedirectView paypalSuccess(HttpServletRequest request) {
        log.info("========== PAYPAL SUCCESS ==========");
        Map<String, String> params = extractParams(request);
        Map<String, String> result = paymentService.handleCallback(PaymentGateway.PAYPAL, params);
        return redirectToFrontend(result);
    }
    /**
     * PayPal cancel callback
     */
    @GetMapping("/paypal/cancel")
    public RedirectView paypalCancel(@RequestParam String token, @RequestParam(required = false) String appointmentId) {
        log.info("========== PAYPAL CANCEL ==========");

        Map<String, String> result = new HashMap<>();
        result.put("status", "cancelled");
        result.put("message", "Thanh toán bị hủy");
        result.put("appointmentId", appointmentId != null ? appointmentId : "");

        return redirectToFrontend(result);
    }

    @LogTime
    @GetMapping("/vnpay_ipn")
    public VNPayIpnResponse vnpayIpn(HttpServletRequest request) {
        log.info("========== VNPAY IPN ==========");
        Map<String, String> params = extractParams(request);
        return paymentService.processVNPayIPN(params);
    }
    @PostMapping("/paypal/webhook")
    public ApiResponse<Void> paypalWebhook(@RequestBody String payload) {
        log.info("========== PAYPAL WEBHOOK ==========");
        // Xử lý bất đồng bộ hoặc đồng bộ tùy nhu cầu
        paymentService.processPayPalWebhook(payload);

        // Luôn trả về 200 OK để PayPal biết mình đã nhận tin
        return ApiResponse.<Void>builder().message("Received").build();
    }

    // Helper methods
    private Map<String, String> extractParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();

        while (paramNames.hasMoreElements()) {
            String name = paramNames.nextElement();
            params.put(name, request.getParameter(name));
        }

        return params;
    }

    private RedirectView redirectToFrontend(Map<String, String> result) {
        String status = result.get("status");
        String appointmentId = result.getOrDefault("appointmentId", "");
        String message = result.getOrDefault("message", "");

        try {
            message = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            // keep original message
        }

        String redirectUrl = String.format(
                "http://localhost:3000/payment-result?status=%s&appointmentId=%s&message=%s",
                status, appointmentId, message);

        log.info("Redirecting to: {}", redirectUrl);
        return new RedirectView(redirectUrl);
    }
}
