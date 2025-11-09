package com.hieu.Booking_System.service.payment;

import com.hieu.Booking_System.configuration.PayPalConfig;
import com.hieu.Booking_System.entity.AppointmentEntity;
import com.hieu.Booking_System.entity.PaymentEntity;
import com.hieu.Booking_System.enums.PaymentStatus;
import com.hieu.Booking_System.repository.AppointmentRepository;
import com.hieu.Booking_System.repository.PaymentRepository;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Component
@Slf4j
@RequiredArgsConstructor
public class PayPalStrategy implements PaymentStrategy {
    private final PayPalHttpClient payPalHttpClient;
    private final PayPalConfig payPalConfig;
    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    @Override
    public String createPaymentUrl(Long appointmentId, HttpServletRequest request) throws Exception {
        AppointmentEntity appointment = appointmentRepository.findById(appointmentId)
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
        BigDecimal amountUSD = appointment.getTotalPrice()
                .divide(new BigDecimal("24000"), 2, RoundingMode.HALF_UP);

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
                                .itemTotal(new Money()
                                        .currencyCode("USD")
                                        .value(amountUSD.toString()))));

        // Items
        List<Item> items = new ArrayList<>();
        Item item = new Item()
                .name("Appointment #" + appointmentId)
                .description("Vaccination appointment")
                .sku("APPT-" + appointmentId)
                .unitAmount(new Money()
                        .currencyCode("USD")
                        .value(amountUSD.toString()))
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
            PaymentEntity payment = paymentRepository.findByTransactionId(token)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            // Capture order
            OrdersCaptureRequest ordersCaptureRequest = new OrdersCaptureRequest(token);
            HttpResponse<Order> response = payPalHttpClient.execute(ordersCaptureRequest);
            Order order = response.result();

            if ("COMPLETED".equals(order.status())) {
                payment.setPaymentStatus(PaymentStatus.COMPLETED);
                paymentRepository.save(payment);

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

    @Override
    public String getGatewayName() {
        return "PAYPAL";
    }
}
