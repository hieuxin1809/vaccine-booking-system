package com.hieu.Booking_System.service.payment;

import com.hieu.Booking_System.configuration.VNPayConfig;
import com.hieu.Booking_System.entity.AppointmentEntity;
import com.hieu.Booking_System.entity.PaymentEntity;
import com.hieu.Booking_System.enums.PaymentStatus;
import com.hieu.Booking_System.repository.AppointmentRepository;
import com.hieu.Booking_System.repository.PaymentRepository;
import com.hieu.Booking_System.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class VNPayStrategy implements PaymentStrategy{
    private final VNPayConfig vnPayConfig;
    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    @Override
    public String createPaymentUrl(Long appointmentId, HttpServletRequest request) throws Exception {
        AppointmentEntity appointment = appointmentRepository.findById(appointmentId)
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

            PaymentEntity payment = paymentRepository.findByTransactionId(vnp_TxnRef)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            if ("00".equals(vnp_ResponseCode)) {
                payment.setPaymentStatus(PaymentStatus.COMPLETED);
                payment.setTransactionId(vnp_TransactionNo);
                paymentRepository.save(payment);

                result.put("status", "success");
                result.put("message", "Thanh toán thành công");
            } else {
                payment.setPaymentStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);

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

    @Override
    public String getGatewayName() {
        return "VNPAY";
    }
}
