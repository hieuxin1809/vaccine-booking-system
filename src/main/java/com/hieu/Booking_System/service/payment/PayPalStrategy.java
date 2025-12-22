package com.hieu.Booking_System.service.payment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hieu.Booking_System.configuration.PayPalConfig;
import com.hieu.Booking_System.entity.AppointmentEntity;
import com.hieu.Booking_System.entity.PaymentEntity;
import com.hieu.Booking_System.entity.UserEntity;
import com.hieu.Booking_System.enums.AppointmentStatus;
import com.hieu.Booking_System.enums.PaymentStatus;
import com.hieu.Booking_System.exception.AppException;
import com.hieu.Booking_System.exception.ErrorCode;
import com.hieu.Booking_System.repository.AppointmentRepository;
import com.hieu.Booking_System.repository.PaymentRepository;
import com.hieu.Booking_System.repository.UserRepository;
import com.hieu.Booking_System.service.BrevoEmailService;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class PayPalStrategy implements PaymentStrategy {
    private final PayPalHttpClient payPalHttpClient;
    private final PayPalConfig payPalConfig;
    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final BrevoEmailService brevoEmailService;
    private final ObjectMapper objectMapper;

    @Override
    public String createPaymentUrl(Long appointmentId, HttpServletRequest request) throws Exception {
        AppointmentEntity appointment = appointmentRepository
                .findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Tạo payment record
        PaymentEntity payment = PaymentEntity.builder()
                .appointmentId(appointmentId)
                .amount(appointment.getTotalPrice())
                .paymentMethod("PAYPAL")
                .paymentStatus(PaymentStatus.PENDING)
                .paymentGateway("PAYPAL")
                .build();
        paymentRepository.save(payment);

        // Convert VND to USD (tỷ giá xấp xỉ 1 USD = 24,000 VND)
        BigDecimal amountUSD = appointment.getTotalPrice().divide(new BigDecimal("24000"), 2, RoundingMode.HALF_UP);

        // Tạo PayPal Order
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.checkoutPaymentIntent("CAPTURE");

        // Application context
        ApplicationContext applicationContext = new ApplicationContext()
                .brandName("Booking System")
                .landingPage("BILLING")
                .cancelUrl(payPalConfig.getCancelUrl() + "?appointmentId=" + appointmentId)
                .returnUrl(payPalConfig.getSuccessUrl() + "?appointmentId=" + appointmentId)
                .userAction("PAY_NOW")
                .shippingPreference("NO_SHIPPING");
        orderRequest.applicationContext(applicationContext);

        // Purchase units
        List<PurchaseUnitRequest> purchaseUnits = new ArrayList<>();
        PurchaseUnitRequest purchaseUnitRequest = new PurchaseUnitRequest()
                .referenceId(appointmentId.toString())
                .description("Payment for appointment #" + appointmentId)
                .customId(String.valueOf(payment.getId()))
                .softDescriptor("BookingSystem")
                .amountWithBreakdown(new AmountWithBreakdown()
                        .currencyCode("USD")
                        .value(amountUSD.toString())
                        .amountBreakdown(new AmountBreakdown()
                                .itemTotal(new Money().currencyCode("USD").value(amountUSD.toString()))));

        // Items
        List<Item> items = new ArrayList<>();
        Item item = new Item()
                .name("Appointment #" + appointmentId)
                .description("Vaccination appointment")
                .sku("APPT-" + appointmentId)
                .unitAmount(new Money().currencyCode("USD").value(amountUSD.toString()))
                .quantity("1")
                .category("DIGITAL_GOODS");
        items.add(item);
        purchaseUnitRequest.items(items);

        purchaseUnits.add(purchaseUnitRequest);
        orderRequest.purchaseUnits(purchaseUnits);

        // Create order
        OrdersCreateRequest ordersCreateRequest = new OrdersCreateRequest();
        ordersCreateRequest.requestBody(orderRequest);

        HttpResponse<Order> response = payPalHttpClient.execute(ordersCreateRequest);
        Order order = response.result();

        // Lưu PayPal Order ID
        payment.setTransactionId(order.id());
        paymentRepository.save(payment);

        // Lấy approval URL
        String approvalUrl = order.links().stream()
                .filter(link -> "approve".equals(link.rel()))
                .findFirst()
                .map(LinkDescription::href)
                .orElseThrow(() -> new RuntimeException("Approval URL not found"));

        log.info("✓ Created PayPal order: {}", order.id());
        return approvalUrl;
    }

    @Transactional
    public void processPayPalWebhook(String payload) {
        try {
            // 1. Parse JSON từ PayPal
            JsonNode root = objectMapper.readTree(payload);
            String eventType = root.path("event_type").asText();
            JsonNode resource = root.path("resource");

            log.info("Handling PayPal Webhook Event: {}", eventType);

            // 2. Chỉ xử lý sự kiện Khách đã duyệt thanh toán (CHECKOUT.ORDER.APPROVED)
            if ("CHECKOUT.ORDER.APPROVED".equals(eventType)) {
                String orderId = resource.path("id").asText();

                // Tìm Payment trong DB bằng TransactionId (Order ID của PayPal)
                PaymentEntity payment = paymentRepository
                        .findByTransactionId(orderId)
                        .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

                // Kiểm tra nếu đã hoàn thành rồi thì bỏ qua
                if (payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
                    log.info("Payment already completed for Order ID: {}", orderId);
                    return;
                }

                // 3. Thực hiện Capture (Trừ tiền ngay lập tức)
                OrdersCaptureRequest ordersCaptureRequest = new OrdersCaptureRequest(orderId);
                HttpResponse<Order> response = payPalHttpClient.execute(ordersCaptureRequest);
                Order order = response.result();

                AppointmentEntity appointment = appointmentRepository
                        .findById(payment.getAppointmentId())
                        .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

                if ("COMPLETED".equals(order.status())) {
                    payment.setPaymentStatus(PaymentStatus.COMPLETED);
                    appointment.setAppointmentStatus(AppointmentStatus.COMPLETED);

                    paymentRepository.save(payment);
                    appointmentRepository.save(appointment);

                    log.info("Successfully captured PayPal payment via Webhook: {}", orderId);
                }
            }
        } catch (Exception e) {
            log.error("PayPal Webhook Error", e);
            // Không throw exception để PayPal không gửi lại (retry) liên tục nếu lỗi logic
        }
    }

    @Transactional
    @Override
    public Map<String, String> handleCallback(Map<String, String> params) {
        Map<String, String> result = new HashMap<>();

        try {
            String token = params.get("token"); // PayPal order ID

            if (token == null || token.isEmpty()) {
                result.put("status", "error");
                result.put("message", "Missing PayPal token");
                return result;
            }

            // Tìm payment
            PaymentEntity payment = paymentRepository
                    .findByTransactionId(token)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            // Capture order
            OrdersCaptureRequest ordersCaptureRequest = new OrdersCaptureRequest(token);
            HttpResponse<Order> response = payPalHttpClient.execute(ordersCaptureRequest);
            Order order = response.result();

            if ("COMPLETED".equals(order.status())) {
                payment.setPaymentStatus(PaymentStatus.COMPLETED);
                paymentRepository.save(payment);
                // GỬI EMAIL XÁC NHẬN SAU KHI THANH TOÁN THÀNH CÔNG
                sendConfirmationEmail(payment);

                result.put("status", "success");
                result.put("message", "Thanh toán PayPal thành công");
            } else {
                payment.setPaymentStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);

                result.put("status", "failed");
                result.put("message", "Thanh toán thất bại: " + order.status());
            }

            result.put("appointmentId", String.valueOf(payment.getAppointmentId()));
            result.put("amount", payment.getAmount().toString());

        } catch (Exception e) {
            log.error("Error processing PayPal callback", e);
            result.put("status", "error");
            result.put("message", e.getMessage());
        }

        return result;
    }

    private void sendConfirmationEmail(PaymentEntity payment) {
        try {
            // Lấy thông tin appointment
            AppointmentEntity appointment = appointmentRepository
                    .findById(payment.getAppointmentId())
                    .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

            // Lấy thông tin user (giả sử AppointmentEntity có trường userId hoặc user)
            UserEntity user = userRepository
                    .findById(appointment.getUser().getId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            // Gửi email
            brevoEmailService.sendAppointmentConfirmationEmail(appointment, payment, user);

            log.info("✓ Đã gửi email xác nhận cho appointment: {}", appointment.getId());
        } catch (Exception e) {
            log.error("✗ Lỗi khi gửi email xác nhận: {}", e.getMessage());
            // Không throw exception để không ảnh hưởng đến luồng thanh toán
        }
    }

    @Override
    public String getGatewayName() {
        return "PAYPAL";
    }
}
