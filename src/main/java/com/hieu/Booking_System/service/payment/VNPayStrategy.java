package com.hieu.Booking_System.service.payment;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.hieu.Booking_System.configuration.VNPayConfig;
import com.hieu.Booking_System.entity.AppointmentEntity;
import com.hieu.Booking_System.entity.PaymentEntity;
import com.hieu.Booking_System.entity.UserEntity;
import com.hieu.Booking_System.enums.AppointmentStatus;
import com.hieu.Booking_System.enums.PaymentStatus;
import com.hieu.Booking_System.exception.AppException;
import com.hieu.Booking_System.exception.ErrorCode;
import com.hieu.Booking_System.model.response.VNPayIpnResponse;
import com.hieu.Booking_System.repository.AppointmentRepository;
import com.hieu.Booking_System.repository.PaymentRepository;
import com.hieu.Booking_System.repository.UserRepository;
import com.hieu.Booking_System.service.BrevoEmailService;
import com.hieu.Booking_System.util.VNPayUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class VNPayStrategy implements PaymentStrategy {
    private final VNPayConfig vnPayConfig;
    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final BrevoEmailService emailService;

    @Override
    public String createPaymentUrl(Long appointmentId, HttpServletRequest request) throws Exception {
        AppointmentEntity appointment = appointmentRepository
                .findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        PaymentEntity payment = PaymentEntity.builder()
                .appointmentId(appointmentId)
                .amount(appointment.getTotalPrice())
                .paymentMethod("VNPAY")
                .paymentStatus(PaymentStatus.PENDING)
                .paymentGateway("VNPAY")
                .build();
        paymentRepository.save(payment);

        String vnp_TxnRef = VNPayUtil.getRandomNumber(8);
        String vnp_IpAddr = VNPayUtil.getIpAddress(request);

        long amount = appointment.getTotalPrice().multiply(new BigDecimal(100)).longValue();

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", VNPayConfig.VERSION);
        vnp_Params.put("vnp_Command", VNPayConfig.COMMAND);
        vnp_Params.put("vnp_TmnCode", vnPayConfig.getVnpTmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "payment" + appointmentId);
        vnp_Params.put("vnp_OrderType", VNPayConfig.ORDER_TYPE);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getVnpReturnUrl());
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();

        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                //     hashData.append(fieldValue);
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getVnpHashSecret(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = vnPayConfig.getVnpApiUrl() + "?" + queryUrl;

        payment.setTransactionId(vnp_TxnRef);
        paymentRepository.save(payment);

        log.info("✓ Created VNPay payment URL for appointment: {}", appointmentId);
        return paymentUrl;
    }

    @Override
    @Transactional
    public Map<String, String> handleCallback(Map<String, String> params) {
        Map<String, String> result = new HashMap<>();

        try {
            String vnp_SecureHash = params.get("vnp_SecureHash");
            params.remove("vnp_SecureHashType");
            params.remove("vnp_SecureHash");

            String signValue = VNPayUtil.hashAllFields(params, vnPayConfig.getVnpHashSecret());

            if (!signValue.equals(vnp_SecureHash)) {
                result.put("status", "error");
                result.put("message", "Chữ ký không hợp lệ");
                return result;
            }

            String vnp_ResponseCode = params.get("vnp_ResponseCode");
            String vnp_TxnRef = params.get("vnp_TxnRef");
            String vnp_TransactionNo = params.get("vnp_TransactionNo");

            PaymentEntity payment = paymentRepository
                    .findByTransactionId(vnp_TxnRef)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            if (payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
                result.put("status", "success");
                result.put("message", "Thanh toán thành công");
                result.put("appointmentId", String.valueOf(payment.getAppointmentId()));
                result.put("amount", payment.getAmount().toString());
                return result;
            }

            AppointmentEntity appointment = appointmentRepository
                    .findById(payment.getAppointmentId())
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));

            if ("00".equals(vnp_ResponseCode)) {
                payment.setPaymentStatus(PaymentStatus.COMPLETED);
                //   payment.setTransactionId(vnp_TransactionNo);
                paymentRepository.save(payment);

                appointment.setAppointmentStatus(AppointmentStatus.COMPLETED);
                appointmentRepository.save(appointment);

                // GỬI EMAIL XÁC NHẬN SAU KHI THANH TOÁN THÀNH CÔNG
                sendConfirmationEmail(payment);

                result.put("status", "success");
                result.put("message", "Thanh toán thành công");
            } else {
                payment.setPaymentStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);

                appointment.setAppointmentStatus(AppointmentStatus.CANCELLED);
                appointmentRepository.save(appointment);

                result.put("status", "failed");
                result.put("message", "Thanh toán thất bại");
            }

            result.put("appointmentId", String.valueOf(payment.getAppointmentId()));
            result.put("amount", payment.getAmount().toString());

        } catch (Exception e) {
            log.error("Error processing VNPay callback", e);
            result.put("status", "error");
            result.put("message", e.getMessage());
        }

        return result;
    }

    @Transactional
    public VNPayIpnResponse processIPN(Map<String, String> params) {
        try {
            // 1. Kiểm tra Checksum
            String vnp_SecureHash = params.get("vnp_SecureHash");
            params.remove("vnp_SecureHashType");
            params.remove("vnp_SecureHash");

            String signValue = VNPayUtil.hashAllFields(params, vnPayConfig.getVnpHashSecret());
            if (!signValue.equals(vnp_SecureHash)) {
                return VNPayIpnResponse.builder()
                        .RspCode("97")
                        .Message("Invalid Checksum")
                        .build();
            }

            // 2. Lấy dữ liệu
            String vnp_TxnRef = params.get("vnp_TxnRef");
            String vnp_ResponseCode = params.get("vnp_ResponseCode");
            String vnp_Amount = params.get("vnp_Amount"); // Số tiền nhân 100

            // 3. Tìm Payment
            PaymentEntity payment = paymentRepository
                    .findByTransactionId(vnp_TxnRef)
                    .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
            if (payment == null) {
                return VNPayIpnResponse.builder()
                        .RspCode("01")
                        .Message("Order not found")
                        .build();
            }

            // 4. Kiểm tra số tiền (Chống hack sửa giá)
            long amountInVnp = payment.getAmount().multiply(new BigDecimal(100)).longValue();
            if (amountInVnp != Long.parseLong(vnp_Amount)) {
                return VNPayIpnResponse.builder()
                        .RspCode("04")
                        .Message("Invalid Amount")
                        .build();
            }

            // 5. Kiểm tra trạng thái đơn hàng (Idempotency - tránh update 2 lần)
            if (payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
                return VNPayIpnResponse.builder()
                        .RspCode("02")
                        .Message("Order already confirmed")
                        .build();
            }

            // 6. Xử lý kết quả
            AppointmentEntity appointment = appointmentRepository
                    .findById(payment.getAppointmentId())
                    .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

            if ("00".equals(vnp_ResponseCode)) {
                // Success
                payment.setPaymentStatus(PaymentStatus.COMPLETED);
                //      payment.setTransactionId(params.get("vnp_TransactionNo"));
                appointment.setAppointmentStatus(AppointmentStatus.COMPLETED);

                paymentRepository.save(payment);
                appointmentRepository.save(appointment);

                // Gửi email
                sendConfirmationEmail(payment);
            } else {
                // Failed
                payment.setPaymentStatus(PaymentStatus.FAILED);
                appointment.setAppointmentStatus(AppointmentStatus.CANCELLED);

                paymentRepository.save(payment);
                appointmentRepository.save(appointment);
            }

            return VNPayIpnResponse.builder()
                    .RspCode("00")
                    .Message("Confirm Success")
                    .build();

        } catch (Exception e) {
            log.error("IPN Error", e);
            return VNPayIpnResponse.builder()
                    .RspCode("99")
                    .Message("Unknown error")
                    .build();
        }
    }
    /**
     * Gửi email xác nhận đặt lịch thành công
     */
    private void sendConfirmationEmail(PaymentEntity payment) {
        try {
            // Lấy thông tin appointment
            AppointmentEntity appointment = appointmentRepository
                    .findById(payment.getAppointmentId())
                    .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

            // Lấy thông tin user
            UserEntity user = userRepository
                    .findById(appointment.getUser().getId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            // Gửi email
            emailService.sendAppointmentConfirmationEmail(appointment, payment, user);

            log.info("✓ Đã gửi email xác nhận cho appointment: {}", appointment.getId());
        } catch (Exception e) {
            log.error("✗ Lỗi khi gửi email xác nhận: {}", e.getMessage());
            // Không throw exception để không ảnh hưởng đến luồng thanh toán
        }
    }

    @Override
    public String getGatewayName() {
        return "VNPAY";
    }
}
